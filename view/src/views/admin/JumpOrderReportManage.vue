 <template>
    <div class="container">
        <div class="top-header">
            <div class="nav-left">
                <Tab :buttons="[
                    { label: '全部', value: 'null' },
                    { label: '待审核', value: '0' },
                    { label: '已扣分', value: '1' },
                    { label: '已驳回', value: '2' },
                ]" initialActive="null" @change="handleChange" />
            </div>
        </div>

        <div class="house-container">
            <div>
                <el-table :data="apiResult.data">
                    <el-table-column prop="agentName" label="举报中介"></el-table-column>
                    <el-table-column prop="username" label="被举报用户"></el-table-column>
                    <el-table-column prop="houseName" label="关联房源">
                        <template #default="scope">
                            <div class="over-text">{{ scope.row.houseName }}</div>
                        </template>
                    </el-table-column>
                    <el-table-column prop="content" label="举报说明" show-overflow-tooltip></el-table-column>
                    <el-table-column prop="status" label="状态" width="100">
                        <template #default="scope">
                            <el-tag size="mini" v-if="scope.row.status === 0">待审核</el-tag>
                            <el-tag size="mini" v-else-if="scope.row.status === 1" type="success">已扣分</el-tag>
                            <el-tag size="mini" v-else-if="scope.row.status === 2" type="danger">已驳回</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="createTime" label="举报时间" width="160"></el-table-column>
                    <el-table-column label="操作" width="100" align="center">
                        <template #default="scope">
                            <el-button type="text" @click="openReview(scope.row)">审核</el-button>
                        </template>
                    </el-table-column>
                </el-table>
                <div class="pager">
                    <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                        :current-page="queryDto.current" :page-sizes="[10, 20]"
                        :page-size="queryDto.size" layout="total, sizes, prev, pager, next, jumper"
                        :total="apiResult.total"></el-pagination>
                </div>
            </div>
        </div>

        <!-- 审核弹窗 -->
        <el-dialog title="举报审核" :visible.sync="dialogVisible" width="500px">
            <el-form :model="reviewForm" label-width="80px">
                <el-form-item label="举报说明">
                    <div>{{ currentItem.content }}</div>
                </el-form-item>
                <el-form-item label="证据图片">
                    <img v-if="currentItem.evidence" :src="currentItem.evidence" style="max-width: 100%; max-height: 200px; border-radius: 4px;" @click="openImage(currentItem.evidence)">
                    <div v-else>无图片证据</div>
                </el-form-item>
                <el-form-item label="审核意见">
                    <el-input type="textarea" v-model="reviewForm.adminComment" placeholder="请输入审核意见"></el-input>
                </el-form-item>
                <el-form-item label="审核结果">
                    <el-radio-group v-model="reviewForm.status">
                        <el-radio :label="1">属实(扣20分)</el-radio>
                        <el-radio :label="2">驳回</el-radio>
                    </el-radio-group>
                </el-form-item>
            </el-form>
            <span slot="footer" class="dialog-footer">
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitReview">确定</el-button>
            </span>
        </el-dialog>
        
        <el-dialog :visible.sync="imageDialogVisible">
             <img width="100%" :src="dialogImageUrl" alt="">
        </el-dialog>
    </div>
</template>

<script>
import Tab from "@/components/Tab"

export default {
    components: { Tab },
    data() {
        return {
            apiResult: {
                data: [],
                total: 0,
            },
            queryDto: {
                current: 1,
                size: 10,
                status: null
            },
            dialogVisible: false,
            imageDialogVisible: false,
            dialogImageUrl: '',
            currentItem: {},
            reviewForm: {
                id: null,
                status: 1,
                adminComment: ''
            }
        };
    },
    created() {
        this.fetchFreshData();
    },
    methods: {
        handleChange(obj) {
            this.queryDto.status = obj.value === 'null' ? null : Number(obj.value);
            this.queryDto.current = 1;
            this.fetchFreshData();
        },
        async fetchFreshData() {
            try {
                const { data, total } = await this.$axios.post('/jump-order-report/list', this.queryDto);
                this.apiResult.data = data;
                this.apiResult.total = total;
            } catch (error) {
                this.$message.error(error.message);
            }
        },
        handleSizeChange(size) {
            this.queryDto.size = size;
            this.fetchFreshData();
        },
        handleCurrentChange(current) {
            this.queryDto.current = current;
            this.fetchFreshData();
        },
        openReview(row) {
            this.currentItem = row;
            this.reviewForm = {
                id: row.id,
                status: 1,
                adminComment: ''
            };
            this.dialogVisible = true;
        },
        openImage(url) {
            this.dialogImageUrl = url;
            this.imageDialogVisible = true;
        },
        async submitReview() {
            try {
                if(this.currentItem.status !== 0) {
                     this.$message.warning("该记录已审核");
                     return;
                }
                await this.$axios.put('/jump-order-report/review', this.reviewForm);
                this.$message.success("审核完成");
                this.dialogVisible = false;
                this.fetchFreshData();
            } catch (error) {
                this.$message.error(error.message);
            }
        }
    }
};
</script>

<style scoped>
.container {
    padding: 20px;
    background-color: #fff;
}
.pager {
    display: flex;
    justify-content: left;
    margin-top: 20px;
}
.over-text {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
</style>
