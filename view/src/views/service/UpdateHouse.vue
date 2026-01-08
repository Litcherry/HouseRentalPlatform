<template>
    <div class="house-container">
        <div class="nav">
            <el-breadcrumb separator-class="el-icon-arrow-right">
                <el-breadcrumb-item style="cursor: pointer;" @click.native="toLastPage">
                    <span style="color: rgb(51,51,51);font-size: 18px;">房源管理</span>
                </el-breadcrumb-item>
                <el-breadcrumb-item>
                    <span style="color: rgb(51,51,51);font-size: 18px;">房屋信息修改</span>
                </el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <!-- 房屋信息区域 -->
        <div class="update-area">
            <!-- 房屋名称 -->
            <div class="area">
                <span>房屋名</span>
                <el-input style="width: 600px;" v-model="house.name" placeholder="请输入，30个字以内"></el-input>
            </div>
            <!-- 省份信息 -->
            <div class="area">
                <span>所属位置</span>
                <div style="display: flex;gap: 20px;">
                    <el-select @change="handleAreaChange" style="width: 290px;" v-model="topAreaId" placeholder="请选择">
                        <el-option v-for="item in topArea" :key="item.id" :label="item.name" :value="item.id">
                        </el-option>
                    </el-select>
                    <el-select @change="fetchCommunity" style="width: 290px;" v-model="cityAreaId" placeholder="请选择">
                        <el-option v-for="item in cityArea" :key="item.id" :label="item.name" :value="item.id">
                        </el-option>
                    </el-select>
                </div>
            </div>
            <!-- 封面 -->
            <div class="area">
                <span>封面</span>
                <div class="user-avatar">
                    <p style="font-size: 12px;color: rgb(0, 119, 184);">点击📷处即可上传房屋封面</p>
                    <img v-if="cover" style="width: 290px;height: 170px;border-radius: 5px;" :src="cover || ''" alt="">
                    <el-upload class="avatar-uploader"
                        action="http://localhost:21090/api/v1.0/house-rental-api/file/upload" :show-file-list="false"
                        :on-success="handleImageSuccess">
                        <i class="el-icon-camera-solid"></i>
                    </el-upload>
                </div>
            </div>
            <!-- 实况图 -->
            <div class="area">
                <span>实况图</span>
                <div>
                    <el-upload :file-list="coverList" :on-success="handleCovers"
                        action="http://localhost:21090/api/v1.0/house-rental-api/file/upload" list-type="picture-card"
                        :on-preview="handlePictureCardPreview" :on-remove="handleRemove">
                        <i class="el-icon-plus"></i>
                    </el-upload>
                    <el-dialog :modal="false" :visible.sync="dialogVisible">
                        <img style="z-index: 1000;" width="100%" :src="dialogImageUrl" alt="">
                    </el-dialog>
                </div>
            </div>
            <!-- 3D模型上传（真3D漫游） -->
            <div class="area">
                <span>3D模型文件</span>
                <div>
                    <p style="font-size: 12px;color: rgb(0, 119, 184);">上传GLTF/GLB格式的3D模型文件（用于真3D漫游看房，推荐使用GLB格式）</p>
                    <el-upload
                        :file-list="model3dList"
                        :on-success="handleModel3d"
                        :on-remove="handleModel3dRemove"
                        :before-upload="beforeModel3dUpload"
                        action="http://localhost:21090/api/v1.0/house-rental-api/file/upload"
                        :limit="1"
                        accept=".gltf,.glb">
                        <el-button size="small" type="primary">点击上传3D模型</el-button>
                        <div slot="tip" class="el-upload__tip">只能上传GLTF/GLB文件，且不超过50MB</div>
                    </el-upload>
                    <div v-if="house.model3dUrl" style="margin-top: 10px; color: #67c23a;">
                        <i class="el-icon-success"></i> 已上传: {{ house.model3dUrl }}
                    </div>
                </div>
            </div>

            <!-- 全景图片上传（360度全景，备用） -->
            <div class="area">
                <span>全景图片（可选）</span>
                <div>
                    <p style="font-size: 12px;color: rgb(0, 119, 184);">上传360度全景图片，支持多个（如果没有3D模型，可使用此功能）</p>
                    <el-upload :file-list="panoramaList" :on-success="handlePanorama"
                        action="http://localhost:21090/api/v1.0/house-rental-api/file/upload" list-type="picture-card"
                        :on-preview="handlePictureCardPreview" :on-remove="handlePanoramaRemove">
                        <i class="el-icon-plus"></i>
                    </el-upload>
                    <el-dialog :modal="false" :visible.sync="dialogVisible">
                        <img style="z-index: 1000;" width="100%" :src="dialogImageUrl" alt="">
                    </el-dialog>
                </div>
            </div>

            <!-- 房屋介绍 -->
            <div class="area">
                <span>房屋介绍</span>
                <div style="background-color: rgba(35, 170, 242,0.2);padding: 1px;">
                    <Editor style="width: 600px;" :receiveContent="content" height="300px"
                        api="http://localhost:21090/api/v1.0/house-rental-api/file/upload" @on-listener="onListener" />
                </div>
            </div>
            <!-- 所属小区 -->
            <div class="area">
                <span>所属小区</span>
                <div style="display: flex;gap: 20px;">
                    <el-select @change="fetchCommunity" style="width: 290px;" v-model="house.communityId"
                        placeholder="请选择">
                        <el-option v-for="item in communityList" :key="item.id" :label="item.name" :value="item.id">
                        </el-option>
                    </el-select>
                </div>
            </div>
            <!-- 房屋类型 -->
            <div class="area">
                <span>房屋类型</span>
                <el-radio-group aria-hidden="false" v-model="house.typeId">
                    <el-radio v-for="item in houseTypeList" :key="item.value" :label="item.value">{{ item.label
                    }}</el-radio>
                </el-radio-group>
            </div>
            <!-- 房屋朝向 -->
            <div class="area">
                <span>房屋朝向</span>
                <el-radio-group aria-hidden="false" v-model="house.directionId">
                    <el-radio v-for="item in houseDirectionList" :key="item.value" :label="item.value">{{ item.label
                    }}</el-radio>
                </el-radio-group>
            </div>
            <!-- 房屋户型 -->
            <div class="area">
                <span>房屋户型</span>
                <el-radio-group aria-hidden="false" v-model="house.sizedId">
                    <el-radio v-for="item in houseSizedList" :key="item.value" :label="item.value">{{ item.label
                    }}</el-radio>
                </el-radio-group>
            </div>
            <!-- 房屋押金方式 -->
            <div class="area">
                <span>房屋押金方式</span>
                <el-radio-group aria-hidden="false" v-model="house.depositMethodId">
                    <el-radio v-for="item in houseDepositMethodList" :key="item.value" :label="item.value">{{ item.label
                    }}</el-radio>
                </el-radio-group>
            </div>
            <!-- 房屋是否临近地铁 -->
            <div class="area">
                <span>房屋是否临近地铁</span>
                <el-radio-group aria-hidden="false" v-model="house.isSubway">
                    <el-radio v-for="item in houseSubwayList" :key="item.value" :label="item.value">{{ item.label
                        }}</el-radio>
                </el-radio-group>
            </div>
            <div class="area" v-if="house.isSubway === 1">
                <span>地铁线路</span>
                <el-slider style="width: 300px;" :max="10" v-model="house.subwayLine" :step="1" show-stops>
                </el-slider>
            </div>
            <!-- 房屋装修状态 -->
            <div class="area">
                <span>装修状态</span>
                <el-radio-group aria-hidden="false" v-model="house.fitmentStatusId">
                    <el-radio v-for="item in houseFitmentStatusList" :key="item.value" :label="item.value">{{ item.label
                        }}</el-radio>
                </el-radio-group>
            </div>
            <!-- 房屋租赁方式 -->
            <div class="area">
                <span>租赁方式</span>
                <el-radio-group aria-hidden="false" v-model="house.rentalType">
                    <el-radio v-for="item in houseRentalTypeList" :key="item.value" :label="item.value">{{ item.label
                        }}</el-radio>
                </el-radio-group>
            </div>
            <!-- 生活设施项配置 -->
            <div class="area">
                <span>生活设施项配置</span>
                <div class="living">
                    <div class="living-item" v-for="(item, index) in houseLivingFacilityList" :key="index">
                        <div class="text">{{ item.label }}</div>
                        <div>
                            <el-switch v-model="item.selected" active-color="#13ce66" inactive-color="#929292">
                            </el-switch>
                        </div>
                    </div>
                </div>
            </div>
            <!-- 房产证号 -->
            <div class="area">
                <span>房产证号</span>
                <div>
                    <el-input style="width: 600px;" v-model="house.propertyCertificateNo" placeholder="请输入房产证号"></el-input>
                    <p style="font-size: 12px;color: rgb(0, 119, 184);margin-top: 5px;">请输入正确的旧版《房屋所有权证》或新版《不动产权证书》编号</p>
                </div>
            </div>
            <!--产权面积-->
            <div class="area">
                <span>产权面积</span>
                <div>
                    <el-input v-model="house.sizeNumber" placeholder="产权面积"></el-input>
                </div>
            </div>
            <!--所属楼层-->
            <div class="area">
                <span>所属楼层</span>
                <div style="display: flex;align-items: center;">
                    <el-input v-model="house.floor" placeholder="输入（高/中/低）"></el-input>
                </div>
            </div>
            <!--月租金-->
            <div class="area">
                <span>月租金</span>
                <div>
                    <el-input v-model="house.rent" placeholder="¥租金（元）"></el-input>
                </div>
            </div>
            <div class="area">
                <div class="info-bt" @click="updateHouse"
                    style="text-align: center;width: 200px;margin-left: 130px;margin-top: 30px;">
                    修改房源信息
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import Editor from "@/components/Editor.vue";
import Tab from "@/components/Tab.vue";
export default {
    components: { Editor, Tab },
    name: "CommunityUpdate",
    data() {
        return {
            communityList: [], // 小区数据
            communityInfo: {},
            house: {
                typeId: null, // 类型
                directionId: null, // 朝向
                sizedId: null, // 户型
                depositMethodId: null, // 租金方式
                isSubway: null,  // 是否临近地铁
                fitmentStatusId: null, // 装修状态
                rentalType: null, // 租赁方式
                propertyCertificateNo: '', // 房产证号
                coverList: [],
                panoramaList: [],  // 全景图片列表（用于显示和编辑）
                model3dList: [], // 3D模型文件列表
            },
            name: null,
            cover: '',
            dialogImageUrl: '',
            dialogVisible: false,
            coverList: [],
            topArea: [], //省份信息
            cityArea: [], // 市区信息
            topAreaId: null, // 省份信息
            cityAreaId: null, // 城市ID
            content: '',
            houseTypeList: [], // 房屋类型数组
            houseDirectionList: [], // 房屋朝向数组
            houseSizedList: [], // 房屋户型数组
            houseDepositMethodList: [], // 房屋押金方式数组
            houseSubwayList: [], // 房屋临近地铁状态
            houseFitmentStatusList: [], // 房屋装修状态数组
            houseRentalTypeList: [], // 房屋租赁方式数组
            houseLivingFacilityList: [], // 查询房屋生活设施配置项数组
            houseId: null,
        }
    },
    created() {
        this.fetchTopArea();
        this.fetchHouseType();
        this.fetchHouseDirection();
        this.fetchHouseSized();
        this.fetchHouseDepositMethod();
        this.fetchHouseSubway();
        this.fetchHouseFitmentStatus();
        this.fetchHouseRentalType();
        this.fetchLivingFacilityType();
        this.getPathId();
    },
    methods: {
        // 取得路径上的携带ID
        getPathId() {
            this.houseId = this.$router.history.current.query.houseId;
            this.fetchHouseInfo(this.houseId);
        },
        async fetchHouseInfo(id) {
            try {
                const { data } = await this.$axios.get(`/house/getById/${id}`);
                this.topAreaId = data.topAreaId; // 设置省份
                this.handleAreaChange(this.topAreaId); // 通过省份ID查询下面所有的市区
                this.cityAreaId = data.areaId; // 设置市区
                this.fetchCommunity(this.cityAreaId); // 通过市区ID查询底下全部的小区
                // 确保所有ID字段是数值类型
                this.house = {
                    ...data,
                    typeId: Number(data.typeId),
                    directionId: Number(data.directionId),
                    sizedId: Number(data.sizedId),
                    depositMethodId: Number(data.depositMethodId),
                    fitmentStatusId: Number(data.fitmentStatusId),
                    rentalType: Number(data.rentalType),
                    isSubway: data.isSubway ? 1 : 2
                };
                this.cover = data.cover;
                this.house.isSubway = data.isSubway ? 1 : 2;
                console.log(data);
                // 处理实况图
                this.coverList = (data.covers || [])  // 防止 covers 为 null/undefined
                    .split(',')                       // 再拆分成数组（如 ["url1", "url2"]）
                    .filter(url => url.trim() !== '') // 过滤空字符串
                    .map(url => ({
                        uid: Date.now() + Math.floor(Math.random() * 1000),
                        url: url.trim()               // 去除前后空格
                    }));
                                    // 处理全景图片
                // 加载3D模型URL
                if (data.model3dUrl) {
                    this.model3dList = [{
                        uid: Date.now(),
                        name: data.model3dUrl.split('/').pop(),
                        url: data.model3dUrl
                    }];
                }
                // 加载全景图片
                this.panoramaList = JSON.parse(data.panoramaJson || '[]').map(item => ({
    uid: Date.now() + Math.floor(Math.random() * 1000),
    url: item.url,
    name: item.name || ''
}));
                // 设置房屋详情
                this.content = data.detail;
                // 设置生活设施
                this.houseLivingFacilityList = JSON.parse(data.livingFacilities);
            } catch (error) {
                console.log("查询房屋信息异常：", error);
            }
        },
        // 房屋租赁方式
        handleRentalTypeChange(obj) {
            this.house.rentalType = Number(obj.value);
        },
        // 房屋装修状态
        handleFitmentStatusChange(obj) {
            this.house.fitmentStatusId = Number(obj.value);
        },
        // 房屋是否临近地铁选择
        handleSubwayChange(obj) {
            this.house.isSubway = obj.value;
        },
        // 房屋类型选择
        handleChange(obj) {
            this.house.typeId = Number(obj.value);
        },
        // 朝向选择
        handleDirectionChange(obj) {
            this.house.directionId = Number(obj.value);
        },
        // 户型选择
        handleSizedChange(obj) {
            this.house.sizedId = Number(obj.value);
        },
        async fetchCommunity(cityAreaId) {
            try {
                const { data } = await this.$axios.post('/community/list', { areaId: cityAreaId });
                this.communityList = data;
            } catch (error) {
                console.log("查询市区下面的小区信息异常：", error);
            }
        },
        // 查询房屋类型
        async fetchHouseType() {
            try {
                const { data } = await this.$axios.get('/house/houseTypeList');
                this.houseTypeList = data;
            } catch (error) {
                console.log("查询房屋类型异常：", error);
            }
        },
        // 查询房屋朝向
        async fetchHouseDirection() {
            try {
                const { data } = await this.$axios.get('/house/houseDirectionList');
                this.houseDirectionList = data;
            } catch (error) {
                console.log("查询房屋朝向异常：", error);
            }
        },
        // 查询房屋户型
        async fetchHouseSized() {
            try {
                const { data } = await this.$axios.get('/house/houseSizedList');
                this.houseSizedList = data;
            } catch (error) {
                console.log("查询房屋户型异常：", error);
            }
        },
        // 查询房屋押金方式数组
        async fetchHouseDepositMethod() {
            try {
                const { data } = await this.$axios.get('/house/houseDepositMethodList');
                this.houseDepositMethodList = data;
            } catch (error) {
                console.log("查询房屋户型异常：", error);
            }
        },
        // 查询房屋临近地铁数组
        async fetchHouseSubway() {
            try {
                const { data } = await this.$axios.get('/house/houseSubwayList');
                this.houseSubwayList = data;
                console.log(this.houseSubwayList);

            } catch (error) {
                console.log("查询房屋是否临近地铁异常：", error);
            }
        },
        // 查询房屋装修状态数组
        async fetchHouseFitmentStatus() {
            try {
                const { data } = await this.$axios.get('/house/houseFitmentStatusList');
                this.houseFitmentStatusList = data;
            } catch (error) {
                console.log("查询房屋装修状态异常：", error);
            }
        },
        // 查询房屋租赁方式数组
        async fetchHouseRentalType() {
            try {
                const { data } = await this.$axios.get('/house/houseRentalTypeList');
                this.houseRentalTypeList = data;
            } catch (error) {
                console.log("查询房屋租赁方式异常：", error);
            }
        },
        // 查询房屋生活设施配置项数组
        async fetchLivingFacilityType() {
            try {
                const { data } = await this.$axios.get('/house/houseLivingFacilityList');
                this.houseLivingFacilityList = data;
            } catch (error) {
                console.log("查询房屋生活设施配置项数组异常：", error);
            }
        },

        async updateHouse() {
            try {
                this.house.cover = this.cover;
                this.house.detail = this.content;
                this.house.areaId = this.cityAreaId;
                this.house.covers = this.coverList.length === 0 ? null : this.coverList.map(entity => entity.url).join(',');
                this.house.livingFacilities = JSON.stringify(this.houseLivingFacilityList);
                             
                this.house.panoramaJson = this.panoramaList.length === 0 ? null : JSON.stringify(this.panoramaList.map(item => ({
    url: item.url,
    name: item.name || `全景 ${this.panoramaList.indexOf(item) + 1}`
})));
                const postData = {
                    ...this.house,
                    typeId: Number(this.house.typeId),
                    directionId: Number(this.house.directionId),
                    sizedId: Number(this.house.sizedId),
                    depositMethodId: Number(this.house.depositMethodId),
                    fitmentStatusId: Number(this.house.fitmentStatusId),
                    rentalType: Number(this.house.rentalType),
                    isSubway: Number(this.house.isSubway),
                };
                const { message } = await this.$axios.put('/house/update', postData);
                this.$notify({
                    title: '房屋修改',
                    type: 'success',
                    message: message,
                    position: 'buttom-right',
                    suration: 1000,
                })
                this.toLastPage();
            } catch (error) {
                console.log("修改房屋信息异常：", error);
                this.$notify({
                    title: '房屋修改',
                    type: 'info',
                    message: error.message,
                    position: 'buttom-right',
                    suration: 1000,
                })
            }
        },
        onListener(text) {
            this.content = text;
        },
        async fetchTopArea() {
            try {
                const areaQueryDto = { parentId: 0 }
                const { data } = await this.$axios.post('/area/list', areaQueryDto);
                this.topArea = data;
            } catch (error) {
                console.log("查询省份信息异常：", error);
            }
        },
        async handleAreaChange(topAreaId) {
            try {
                const areaQueryDto = { parentId: topAreaId }
                const { data } = await this.$axios.post('/area/list', areaQueryDto);
                this.cityArea = data;
            } catch (error) {
                console.log("查询省份下的市区信息异常：", error);
            }
        },
             // 处理全景图片上传成功
        handlePanorama(response, file, fileList) {
            this.panoramaList.push({
            uid: Date.now() + Math.floor(Math.random() * 1000),
            url: response.data
            });
        },

        // 处理全景图片移除
        handlePanoramaRemove(file, fileList) {
            this.panoramaList = this.panoramaList.filter(entity => entity.uid !== file.uid);
        },
        // 处理3D模型上传前验证
        beforeModel3dUpload(file) {
            const isGLTF = file.name.toLowerCase().endsWith('.gltf') || file.name.toLowerCase().endsWith('.glb');
            const isLt50M = file.size / 1024 / 1024 < 50;

            if (!isGLTF) {
                this.$message.error('只能上传GLTF/GLB格式的3D模型文件!');
                return false;
            }
            if (!isLt50M) {
                this.$message.error('3D模型文件大小不能超过50MB!');
                return false;
            }
            return true;
        },
        // 处理3D模型上传成功
        handleModel3d(response, file, fileList) {
            this.house.model3dUrl = response.data;
            this.model3dList = [{
                uid: file.uid,
                name: file.name,
                url: response.data
            }];
            this.$message.success('3D模型上传成功!');
        },
        // 处理3D模型移除
        handleModel3dRemove(file, fileList) {
            this.house.model3dUrl = null;
            this.model3dList = [];
        },

        handleCovers(response, file, fileList) {
            this.coverList.push({
                uid: Date.now() + Math.floor(Math.random() * 1000),
                url: response.data
            });
            console.log("上传，此时的图片数组：", this.coverList);
        },
        handlePictureCardPreview(file) {
            this.dialogImageUrl = file.url;
            this.dialogVisible = true;
        },
        handleRemove(file, fileList) {
            console.log("file:", file);

            if (fileList.length === 0) return;
            this.coverList = this.coverList.filter(entity => entity.uid !== file.uid);
        },
        handleImageSuccess(res, file) {
            // 通知提示
            this.$notify({
                title: '封面上传',
                type: res.code === 200 ? 'success' : 'error',
                message: res.code === 200 ? '上传成功' : res.data,
                position: 'buttom-right',
                suration: 1000,
            })
            if (res.code === 200) {
                this.cover = res.data; // 响应里面的data，即后端返回的上传后的图片链接
            }
        },
        toLastPage() {
            this.$router.go(-1); // 返回上一页
        },
    }
};
</script>

<style scoped lang="scss">
.living {
    display: flex;
    flex-wrap: wrap;
    background-color: rgb(246, 247, 248);
    width: 800px;

    .living-item {
        display: flex;
        padding: 10px;
        gap: 10px;

        .text {
            font-size: 12px;
        }
    }
}

.update-area {
    padding-block: 10px;
    background-color: rgb(255, 255, 255);
}

.area {
    display: flex;
    justify-content: left;
    align-items: center;
    gap: 10px;
    margin-top: 6px;
    margin-block: 30px;

    span {
        width: 150px;
        display: inline-block;
        text-align: left;
        margin-right: 10px;
        padding: 0 20px;
        font-size: 16px;
        color: rgb(51, 51, 51);
    }
}

.house-container {

    box-sizing: border-box;

    .nav {
        margin-bottom: 20px;
    }
}
</style>