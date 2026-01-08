<template>
  <div class="container">
    <div class="top-header">
      <div class="nav-left">
        <Tab :buttons="[
          { label: '全部', value: 'null' },
          { label: '未审核', value: '0' },
          { label: '已审核', value: '1' }
        ]" initialActive="null" @change="handleChange" />
      </div>
      <div class="nav-right">
        <div>
          <AutoInput placeholder="搜索身份证号" @listener="listener" />
        </div>
      </div>
    </div>
    <!-- 表格及分页信息 -->
    <div>
      <el-table :data="apiResult.data">
        <el-table-column width="200" prop="username" label="申请人">
          <template #default="scope">
            <div class="over-text">
              {{ scope.row.username }}
            </div>
          </template>
        </el-table-column>
        <el-table-column width="300" prop="content" label="身份证号">
          <template #default="scope">
            <div class="over-text">
              {{ scope.row.idcard }}
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="parentId" :sortable="true" width="108" label="审核状态">
          <template #default="scope">
            <el-tag :type="scope.row.isAudit ? 'success' : 'danger'" size="mini">{{ scope.row.isAudit ? '已审核' : '未审核'
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :sortable="true" width="168" label="申请时间"></el-table-column>
        <el-table-column label="" align="center">
          <template #default="scope">
            <div class="operate-buttons">
              <el-dropdown trigger="click" placement="bottom-end">
                <span class="el-dropdown-link">
                  <i class="el-icon-more"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item @click.native="handleDetail(scope.row)"
                    icon="el-icon-finished">详情</el-dropdown-item>
                  <el-dropdown-item @click.native="handleDelete(scope.row)" icon="el-icon-delete">删除</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页组件区域 -->
      <div class="pager">
        <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
          :current-page="landlordQueryDto.current" :page-sizes="[10, 20]" :page-size="landlordQueryDto.size"
          layout="total, sizes, prev, pager, next, jumper" :total="apiResult.total"></el-pagination>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <el-dialog title="删除房东信息" :show-close="false" :visible.sync="dialogDeletedVisible" width="20%">
      <span>确定删除房东信息数据？</span>
      <span slot="footer" class="dialog-footer">
        <el-button size="mini" @click="dialogDeletedVisible = false">取消</el-button>
        <el-button size="mini" type="primary" @click="confirmDeleted">确定</el-button>
      </span>
    </el-dialog>

    <!-- 房东申请信息抽屉 -->
    <el-drawer :title="drawerTitle" :modal="false" :wrapperClosable="false" :visible.sync="drawer" :direction="direction"
      size="70%" :before-close="handleClose">
      <div style="gap: 30px;display: flex;justify-content: center;align-items: center;">
        <div>
          <img style="width: 80px;height: 80px;border-radius: 50%;" :src="landlord.avatar" alt="" srcset="">
          <div style="text-align: center;font-size: 24px;">{{ landlord.username }}</div>
        </div>
        <div
          style="width: 600px;background-color: rgb(250,250,250);padding: 20px 60px;box-shadow: 0 4px 8px rgb(240,240,240);border-radius: 5px;">
          <div>
            <p>*身份证号</p>
            <div style="font-size: 18px;">{{ landlord.idcard }}</div>
          </div>
          <div style="display: flex;">
            <div>
              <p>*身份证正面照</p>
              <img style="width: 200px;height: 140px;" :src="landlord.idcardFront" alt="">
            </div>
            <div>
              <p>*身份证反面照</p>
              <img style="width: 200px;height: 140px;" :src="landlord.idcardOpposite" alt="">
            </div>
          </div>
        </div>
      </div>

      <!-- 审核操作区域 -->
      <div v-if="!landlord.isAudit" style="text-align: center;margin-top: 30px;padding: 20px;background-color: #f9f9f9;border-radius: 8px;">
        <div v-if="!showRejectForm" style="margin-bottom: 20px;">
          <span @click="auditLandlord" class="approve-btn"><i class="el-icon-check"
              style="margin-right: 5px;"></i>通过审核</span>
          <span @click="showRejectForm = true" class="reject-btn"><i class="el-icon-close"
              style="margin-right: 5px;"></i>打回申请</span>
        </div>

        <!-- 打回表单 -->
        <div v-if="showRejectForm" style="max-width: 400px;margin: 0 auto;">
          <h4 style="margin-bottom: 15px;color: #f56c6c;">打回申请</h4>
          <el-input
            type="textarea"
            v-model="rejectReason"
            placeholder="请输入打回原因..."
            :rows="4"
            maxlength="200"
            show-word-limit>
          </el-input>
          <div style="margin-top: 15px;">
            <el-button type="danger" size="small" @click="confirmReject" :loading="rejecting">
              <i class="el-icon-close" style="margin-right: 5px;"></i>确认打回
            </el-button>
            <el-button size="small" @click="showRejectForm = false; rejectReason = ''">
              <i class="el-icon-back" style="margin-right: 5px;"></i>取消
            </el-button>
          </div>
        </div>
      </div>

      <!-- 已审核状态显示 -->
      <div v-if="landlord.isAudit" style="text-align: center;margin-top: 30px;padding: 20px;background-color: #f0f9ff;border-radius: 8px;">
        <div style="color: #67c23a;font-size: 16px;font-weight: bold;">
          <i class="el-icon-success" style="margin-right: 8px;"></i>该申请已通过审核
        </div>
      </div>
    </el-drawer>

  </div>
</template>

<script>
// B站 「程序辰星」原创出品
import AutoInput from "@/components/AutoInput.vue"; // 自己封装好的输入框组件
import Tab from "@/components/Tab.vue";
export default {
  components: { AutoInput,Tab }, // 注册组件
  data() {
    return {
      drawer: false,
      direction: 'ttb',
      id: null, // 页面即将删除的数据ID
      apiResult: { // 后端返回的查询数据的响应数据
        data: [], // 数据项
        total: 0, // 符合条件的数据总想 - 初始赋值为0
      },
      landlord: {}, // 房东信息
      landlordQueryDto: { // 搜索条件
        current: 1, // 当前页码，从1开始
        size: 10, // 页面显示大小 - 初始是10条
      },
      dialogDeletedVisible: false, // 删除弹窗控制开关变量 - 初始是关（false）
      showRejectForm: false, // 是否显示打回表单
      rejectReason: '', // 打回原因
      rejecting: false, // 是否正在打回操作
    };
  },
  computed: {
    // 动态计算抽屉标题
    drawerTitle() {
      if (this.landlord.isAudit) {
        return '已审核房东信息';
      }
      return '待审核房东申请';
    }
  },
  created() {
    this.fetchFreshData();
  },
  methods: {
    handleChange(val){
      this.landlordQueryDto.isAudit = Number(val.value);
      this.fetchFreshData();
    },
    handleClose() {
      this.drawer = false;
      this.showRejectForm = false;
      this.rejectReason = '';
      this.rejecting = false;
    },
    handleDetail(data) {
      this.drawer = true;
      this.landlord = data;
    },
    // 输入框组件输入回传
    listener(text) {
      this.landlordQueryDto.idcard = text; // 赋值查询条件的内容
      this.fetchFreshData(); // 重新加载数据
    },
    async auditLandlord() {
      try {
        await this.$confirm('确定要通过该房东的审核申请吗？', '确认通过', {
          confirmButtonText: '确定通过',
          cancelButtonText: '取消',
          type: 'success'
        });

        const landlord = {
          id: this.landlord.id,
          isAudit: true,
        }
        await this.$axios.put('/landlord/update', landlord);
        this.$message.success('审核通过成功');
        this.drawer = false; // 关闭详情抽屉
        this.fetchFreshData(); // 重新加载房东数据
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error((error.response && error.response.data && error.response.data.message) || error.message || '审核失败');
          console.error('审核房东信息信息异常:', error);
        }
      }
    },
    // 打回申请
    async confirmReject() {
      if (!this.rejectReason.trim()) {
        this.$message.warning('请输入打回原因');
        return;
      }

      try {
        this.rejecting = true;

        await this.$confirm('确定要打回该房东的申请吗？', '确认打回', {
          confirmButtonText: '确定打回',
          cancelButtonText: '取消',
          type: 'warning'
        });

        // 调用专门的打回API
        await this.$axios.put(`/landlord/reject/${this.landlord.id}`, {
          reason: this.rejectReason
        });

        this.$message.success('已打回申请');
        this.drawer = false; // 关闭详情抽屉
        this.showRejectForm = false; // 隐藏打回表单
        this.rejectReason = ''; // 清空打回原因
        this.fetchFreshData(); // 重新加载房东数据
      } catch (error) {
        if (error !== 'cancel') {
         this.$message.error((error.response && error.response.data && error.response.data.message) || error.message || '打回失败');
          console.error('打回房东申请异常:', error);
        }
      } finally {
        this.rejecting = false;
      }
    },
    // 查询房东信息数据
    async fetchFreshData() {
      try {
        const { data, total } = await this.$axios.post('/landlord/list', this.landlordQueryDto);
        this.apiResult.data = data;
        this.apiResult.total = total;
      } catch (error) {
        console.error('查询房东信息信息异常:', error);
      }
    },
    // 分页 - 处理页面页数切换
    handleSizeChange(size) {
      this.landlordQueryDto.size = size; // 当前页面大小重置
      this.landlordQueryDto.current = 1; // 当前页设置为第一页
      this.fetchFreshData(); // 重新加载页面数据
    },
    // 分页 - 处理页面当前页切换
    handleCurrentChange(current) {
      this.landlordQueryDto.current = current; // 设置当前页码
      this.fetchFreshData(); // 重新加载页面数据
    },
    // 表格点击删除房东信息
    handleDelete(row) {
      this.dialogDeletedVisible = true; // 开启删除弹窗确认
      this.id = row.id;
    },
    // 房东信息删除
    async confirmDeleted() {
      try {
        const { code } = await this.$axios.delete(`/landlord/${this.id}`);
        if (code === 200) {
          this.$notify.success({
            title: '房东信息删除',
            message: '删除成功',
            position: 'buttom-right',
            suration: 1000,
          });
          this.dialogDeletedVisible = false; // 关闭删除确认弹窗
          this.id = null; // 将标识ID置位
          this.fetchFreshData(); // 删除房东信息数据之后，重新加载房东信息数据
        }
      } catch (error) {
        console.log("删除房东信息数据异常：", error);
      }
    }
  },
};
</script>
<style scoped lang="scss">
.pager {
  margin-block: 20px;
}

/* 默认隐藏操作按钮 */
.operate-buttons {
  opacity: 0;
  transition: opacity 0.3s;
  /* 添加过渡效果 */
  cursor: pointer;

  i {
    padding: 8px;
    border-radius: 6px;
    transition: all .5s ease;

    &:hover {
      background-color: rgb(236, 237, 238);
    }
  }

}

/* 行悬停时显示操作按钮 */
.el-table__body tr:hover .operate-buttons {
  opacity: 1;
}

.container {
  margin: 10px 20px;
}

.top-header {
  margin-block: 10px;
  padding-inline: 10px;
  border-radius: 5px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .nav-left,
  .nav-right {
    display: flex;
    justify-content: left;
    align-items: center;
    gap: 10px;
  }

  .nav-left {
    display: flex;
  }

}

/* 审核按钮样式 */
.approve-btn, .reject-btn {
  display: inline-block;
  padding: 10px 20px;
  margin: 0 10px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.approve-btn {
  background-color: #67c23a;
  color: white;
  border: 1px solid #67c23a;

  &:hover {
    background-color: #85ce61;
    border-color: #85ce61;
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(103, 194, 58, 0.3);
  }
}

.reject-btn {
  background-color: #f56c6c;
  color: white;
  border: 1px solid #f56c6c;

  &:hover {
    background-color: #f78989;
    border-color: #f78989;
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(245, 108, 108, 0.3);
  }
}

/* 增强表格样式 */
::v-deep .el-table__row {
  &:hover {
    background-color: #f5f7fa !important;
  }
}

/* 状态标签样式 */
::v-deep .el-tag--success {
  background-color: #f0f9ff;
  border-color: #67c23a;
  color: #67c23a;
}

::v-deep .el-tag--danger {
  background-color: #fef0f0;
  border-color: #f56c6c;
  color: #f56c6c;
}
</style>