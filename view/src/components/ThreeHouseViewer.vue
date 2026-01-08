<template>
  <div class="three-house-viewer">
    <div class="viewer-container" ref="container"></div>
    
    <!-- 操作面板 -->
    <div class="controls-panel">
      <div class="control-hint">
        <div class="mode-status">
          <b>当前模式:</b> 
          <span :class="{ 'text-active': isLocked }">{{ isLocked ? '室内漫游' : '上帝观察' }}</span>
        </div>
        
        <div class="hint-content">
          <template v-if="!isLocked">
            <p>🖱️ <b>左键</b>旋转 / <b>滚轮</b>缩放</p>
            <p>✨ <b>双击地面</b> 锁定降落点并传送</p>
          </template>
          <template v-else>
            <p>⌨️ <b>W A S D</b> 移动 / <b>C</b> 开关碰撞</p>
            <p>🛡️ <b>碰撞模式:</b> 
              <span :style="{color: collisionEnabled ? '#67c23a' : '#f56c6c', fontWeight: 'bold'}">
                {{ collisionEnabled ? '开启' : '关闭' }}
              </span>
            </p>
          </template>
        </div>

        <el-button 
          size="medium" 
          :type="isLocked ? 'danger' : 'primary'" 
          @click="toggleControls" 
          class="action-btn"
        >
          {{ isLocked ? '退出漫游模式' : '从定位点降落' }}
        </el-button>
      </div>
    </div>

    <!-- 右侧跳转菜单 -->
    <div class="room-menu" v-if="!loading && !isLocked">
      <div class="menu-title">视角跳转</div>
      <div class="room-item" @click="quickTeleport('center')">🏠 房屋中心</div>
      <div class="room-item" @click="quickTeleport('north')">⬆️ 北侧房间</div>
      <div class="room-item" @click="quickTeleport('south')">⬇️ 南侧房间</div>
      <div class="room-item" @click="quickTeleport('east')">➡️ 东侧房间</div>
      <div class="room-item" @click="quickTeleport('west')">⬅️ 西侧房间</div>
    </div>

    <div class="loading-overlay" v-if="loading">
      <div class="loading-spinner"></div>
      <div class="loading-text">正在初始化物理引擎... {{ progress }}</div>
    </div>
  </div>
</template>

<script>
import * as THREE from 'three'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { PointerLockControls } from 'three/examples/jsm/controls/PointerLockControls.js'

export default {
  name: 'ThreeHouseViewer',
  props: {
    modelUrl: { type: String, required: true }
  },
  data() {
    return {
      loading: true,
      progress: '0%',
      isLocked: false,
      collisionEnabled: true,
      velocity: new THREE.Vector3(),
      direction: new THREE.Vector3(),
      moveForward: false, moveBackward: false, moveLeft: false, moveRight: false,
      prevTime: performance.now(),
      collidableObjects: [],
      raycaster: new THREE.Raycaster(),
      spawnPoint: new THREE.Vector3(0, 1.65, 0),
      // 新增：手动双击检测变量
      lastClickTime: 0
    }
  },
  mounted() {
    this.init()
    window.addEventListener('resize', this.onWindowResize)
    document.addEventListener('keydown', this.onKeyDown)
    document.addEventListener('keyup', this.onKeyUp)
  },
  beforeDestroy() {
    this.clean()
  },
  methods: {
    init() {
      const container = this.$refs.container
      this.scene = new THREE.Scene()
      this.scene.background = new THREE.Color(0xade0f9)

      this.camera = new THREE.PerspectiveCamera(75, container.clientWidth / container.clientHeight, 0.01, 2000)
      
      this.renderer = new THREE.WebGLRenderer({ antialias: true, logarithmicDepthBuffer: true })
      this.renderer.setSize(container.clientWidth, container.clientHeight)
      this.renderer.setPixelRatio(window.devicePixelRatio)
      this.renderer.outputColorSpace = THREE.SRGBColorSpace
      container.appendChild(this.renderer.domElement)

      // 灯光
      const ambientLight = new THREE.AmbientLight(0xffffff, 1.2)
      this.scene.add(ambientLight)
      const hemiLight = new THREE.HemisphereLight(0xffffff, 0x444444, 1.0)
      this.scene.add(hemiLight)

      // 控制器
      this.orbit = new OrbitControls(this.camera, this.renderer.domElement)
      this.orbit.enableDamping = true

      this.pointerLock = new PointerLockControls(this.camera, this.renderer.domElement)
      
      this.pointerLock.addEventListener('lock', () => {
        this.isLocked = true
        this.orbit.enabled = false
      })
      this.pointerLock.addEventListener('unlock', () => {
        this.isLocked = false
        this.orbit.enabled = true
      })

      // 【核心修改】：手动接管点击事件，绕过 OrbitControls 对双击的干扰
      this.renderer.domElement.addEventListener('mousedown', this.onMouseClick);

      this.loadModel()
      this.animate()
    },

    // 手动双击检测逻辑
    onMouseClick(event) {
      if (this.isLocked) return;

      const currentTime = Date.now();
      const timeDiff = currentTime - this.lastClickTime;

      // 如果两次点击时间差小于 300ms，判定为双击
      if (timeDiff < 300) {
        this.executeTeleport(event);
      }
      this.lastClickTime = currentTime;
    },

    executeTeleport(event) {
      const rect = this.renderer.domElement.getBoundingClientRect();
      const mouse = new THREE.Vector2(
        ((event.clientX - rect.left) / rect.width) * 2 - 1,
        -((event.clientY - rect.top) / rect.height) * 2 + 1
      );

      this.raycaster.setFromCamera(mouse, this.camera);
      const intersects = this.raycaster.intersectObjects(this.collidableObjects);

      if (intersects.length > 0) {
        const point = intersects[0].point;
        // 记录降落点
        this.spawnPoint.set(point.x, 1.65, point.z);
        
        // 视觉反馈
        this.orbit.target.set(point.x, 0, point.z);
        this.camera.position.set(point.x + 4, 10, point.z + 4);
        this.orbit.update();

        this.$message({ message: '位置已锁定', type: 'success', duration: 1000, offset: 100 });
      }
    },

    loadModel() {
      const loader = new GLTFLoader()
      loader.load(this.modelUrl, (gltf) => {
        const model = gltf.scene
        this.collidableObjects = []
        model.traverse(child => {
          if (child.isMesh) {
            child.material.side = THREE.DoubleSide
            this.collidableObjects.push(child)
          }
        })

        const box = new THREE.Box3().setFromObject(model)
        const size = box.getSize(new THREE.Vector3())
        const center = box.getCenter(new THREE.Vector3())
        const scale = 20 / Math.max(size.x, size.y, size.z)
        
        model.scale.set(scale, scale, scale)
        model.position.x = -center.x * scale
        model.position.y = -box.min.y * scale
        model.position.z = -center.z * scale

        this.scene.add(model)
        this.camera.position.set(20, 18, 20)
        this.orbit.target.set(0, 0, 0)
        this.orbit.update()
        this.loading = false
      }, (xhr) => {
        this.progress = Math.round((xhr.loaded / xhr.total) * 100) + '%'
      })
    },

    // 碰撞检测与自动脱卡
    checkCollision(localDir) {
      if (!this.collisionEnabled) return false;

      const rayOrigin = this.camera.position.clone();
      rayOrigin.y = 1.0; 
      const worldDir = localDir.clone().applyQuaternion(this.camera.quaternion);
      worldDir.y = 0; 
      worldDir.normalize();

      this.raycaster.set(rayOrigin, worldDir);
      this.raycaster.far = 0.6;
      const hits = this.raycaster.intersectObjects(this.collidableObjects, true);
      
      for (let hit of hits) {
          if (hit.point.y > 0.2 && hit.point.y < 2.8) {
              // 自动脱卡逻辑：如果太近了，强行推开一点
              if (hit.distance < 0.45) {
                  const push = worldDir.clone().negate().multiplyScalar(0.06);
                  this.camera.position.add(push);
              }
              return true;
          }
      }
      return false;
    },

    quickTeleport(area) {
      const d = 5;
      let target = { x: 0, z: 0 };
      switch(area) {
        case 'north': target.z = -d; break;
        case 'south': target.z = d; break;
        case 'east':  target.x = d; break;
        case 'west':  target.x = -d; break;
      }
      this.spawnPoint.set(target.x, 1.65, target.z);
      this.orbit.target.set(target.x, 0, target.z);
      this.camera.position.set(target.x , 8, target.z );
      this.orbit.update();
    },

    toggleControls() {
      if (!this.isLocked) {
        this.camera.position.copy(this.spawnPoint);
        this.pointerLock.lock();
      } else {
        this.pointerLock.unlock();
      }
    },

    onKeyDown(e) {
      if (e.code === 'KeyC') {
        this.collisionEnabled = !this.collisionEnabled;
        return;
      }
      if (!this.isLocked) return;
      switch (e.code) {
        case 'KeyW': this.moveForward = true; break;
        case 'KeyS': this.moveBackward = true; break;
        case 'KeyA': this.moveLeft = true; break;
        case 'KeyD': this.moveRight = true; break;
      }
    },

    onKeyUp(e) {
      if (!this.isLocked) return;
      switch (e.code) {
        case 'KeyW': this.moveForward = false; break;
        case 'KeyS': this.moveBackward = false; break;
        case 'KeyA': this.moveLeft = false; break;
        case 'KeyD': this.moveRight = false; break;
      }
    },

    updateMovement() {
      if (!this.isLocked) {
        this.orbit.update();
        return;
      }

      const time = performance.now();
      const delta = Math.min((time - this.prevTime) / 1000, 0.1);
      
      this.velocity.x -= this.velocity.x * 12.0 * delta;
      this.velocity.z -= this.velocity.z * 12.0 * delta;

      const dir = new THREE.Vector3();
      dir.z = Number(this.moveForward) - Number(this.moveBackward);
      dir.x = Number(this.moveRight) - Number(this.moveLeft);
      dir.normalize();

      const speed = 110.0;
      if (dir.z !== 0 && !this.checkCollision(new THREE.Vector3(0, 0, dir.z))) {
          this.velocity.z -= dir.z * speed * delta;
      }
      if (dir.x !== 0 && !this.checkCollision(new THREE.Vector3(dir.x, 0, 0))) {
          this.velocity.x -= dir.x * speed * delta;
      }

      this.pointerLock.moveRight(-this.velocity.x * delta);
      this.pointerLock.moveForward(-this.velocity.z * delta);
      this.camera.position.y = 1.65; 
      this.prevTime = time;
    },

    animate() {
      this.req = requestAnimationFrame(this.animate)
      this.updateMovement()
      this.renderer.render(this.scene, this.camera)
    },

    onWindowResize() {
      const container = this.$refs.container
      if (!container) return
      this.camera.aspect = container.clientWidth / container.clientHeight
      this.camera.updateProjectionMatrix()
      this.renderer.setSize(container.clientWidth, container.clientHeight)
    },

    clean() {
      cancelAnimationFrame(this.req)
      if (this.renderer) {
          this.renderer.domElement.removeEventListener('mousedown', this.onMouseClick);
          this.renderer.dispose();
      }
      document.removeEventListener('keydown', this.onKeyDown)
      document.removeEventListener('keyup', this.onKeyUp)
      window.removeEventListener('resize', this.onWindowResize)
    }
  }
}
</script>

<style scoped>
.three-house-viewer { position: relative; width: 100%; height: 100%; background: #ade0f9; overflow: hidden; }
.viewer-container { width: 100%; height: 100%; }

.controls-panel { 
  position: absolute; top: 15px; left: 15px; 
  background: rgba(255, 255, 255, 0.95); padding: 15px; 
  border-radius: 10px; box-shadow: 0 4px 15px rgba(0,0,0,0.2); 
  z-index: 10; width: 220px; 
}
.room-menu {
  position: absolute; top: 15px; right: 15px;
  background: rgba(0, 0, 0, 0.7); color: white;
  padding: 10px; border-radius: 8px; width: 130px;
  z-index: 10;
}
.menu-title { font-weight: bold; margin-bottom: 8px; border-bottom: 1px solid rgba(255,255,255,0.3); padding-bottom: 4px; font-size: 13px; text-align: center;}
.room-item { padding: 6px 8px; margin-bottom: 4px; cursor: pointer; border-radius: 4px; transition: 0.2s; font-size: 12px; }
.room-item:hover { background: #409eff; }
.mode-status { font-size: 14px; margin-bottom: 8px; border-bottom: 1px solid #eee; padding-bottom: 6px; }
.text-active { color: #409eff; font-weight: bold; }
.hint-content { font-size: 11px; color: #666; line-height: 1.8; margin-bottom: 10px; }
.action-btn { width: 100%; font-weight: bold; font-size: 13px; }

.loading-overlay { position: absolute; inset: 0; background: #fff; display: flex; flex-direction: column; justify-content: center; align-items: center; z-index: 100; }
.loading-spinner { width: 40px; height: 40px; border: 4px solid #f3f3f3; border-top: 4px solid #409eff; border-radius: 50%; animation: spin 1s linear infinite; margin-bottom: 10px; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>