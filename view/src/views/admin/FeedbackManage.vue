<template>
    <div class="feedback-manage">
        <div class="page-header">
            <h1>
                <i class="el-icon-chat-line-square"></i>
                问题反馈管理
            </h1>
            <p>管理和处理用户提交的问题反馈</p>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-container">
            <div class="stat-card">
                <div class="stat-icon pending">
                    <i class="el-icon-time"></i>
                </div>
                <div class="stat-content">
                    <div class="stat-number">{{ stats.pending }}</div>
                    <div class="stat-label">待处理</div>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon processing">
                    <i class="el-icon-loading"></i>
                </div>
                <div class="stat-content">
                    <div class="stat-number">{{ stats.processing }}</div>
                    <div class="stat-label">处理中</div>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon resolved">
                    <i class="el-icon-success"></i>
                </div>
                <div class="stat-content">
                    <div class="stat-number">{{ stats.resolved }}</div>
                    <div class="stat-label">已解决</div>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon total">
                    <i class="el-icon-document"></i>
                </div>
                <div class="stat-content">
                    <div class="stat-number">{{ stats.total }}</div>
                    <div class="stat-label">总反馈数</div>
                </div>
            </div>
        </div>

        <!-- 搜索和筛选 -->
        <div class="filter-container">
            <el-form :inline="true" :model="searchForm" class="search-form">
                <el-form-item label="工单号">
                    <el-input
                        v-model="searchForm.workOrderId"
                        placeholder="请输入工单号"
                        clearable
                        style="width: 200px;">
                    </el-input>
                </el-form-item>
                <el-form-item label="反馈类型">
                    <el-select
                        v-model="searchForm.feedbackType"
                        placeholder="请选择"
                        clearable
                        style="width: 150px;">
                        <el-option
                            v-for="item in feedbackTypes"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value">
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="处理状态">
                    <el-select
                        v-model="searchForm.status"
                        placeholder="请选择"
                        clearable
                        style="width: 150px;">
                        <el-option
                            v-for="item in statusOptions"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value">
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="优先级">
                    <el-select
                        v-model="searchForm.priority"
                        placeholder="请选择"
                        clearable
                        style="width: 120px;">
                        <el-option label="高" value="HIGH"></el-option>
                        <el-option label="中" value="MEDIUM"></el-option>
                        <el-option label="低" value="LOW"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="分配给">
                    <el-select
                        v-model="searchForm.assignedTo"
                        placeholder="请选择"
                        clearable
                        style="width: 150px;">
                        <el-option
                            v-for="user in adminUsers"
                            :key="user.id"
                            :label="user.username"
                            :value="user.id">
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="searchFeedbacks">
                        <i class="el-icon-search"></i>
                        搜索
                    </el-button>
                    <el-button @click="resetSearch">
                        <i class="el-icon-refresh"></i>
                        重置
                    </el-button>
                    <el-button type="success" @click="refreshStats">
                        <i class="el-icon-refresh"></i>
                        刷新统计
                    </el-button>
                </el-form-item>
            </el-form>
        </div>

        <!-- 反馈列表 -->
        <div class="feedback-list">
            <div v-if="loading" class="loading-container">
                <el-skeleton :rows="8" animated />
            </div>

            <el-table
                v-else
                :data="feedbackList"
                stripe
                style="width: 100%"
                @row-click="handleRowClick">
                <el-table-column prop="workOrderId" label="工单号" width="150" fixed>
                    <template slot-scope="scope">
                        <span style="font-family: monospace; font-weight: 600;">
                            {{ scope.row.workOrderId }}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip></el-table-column>
                <el-table-column prop="feedbackTypeName" label="反馈类型" width="120">
                    <template slot-scope="scope">
                        <el-tag size="mini">{{ scope.row.feedbackTypeName || scope.row.feedbackType }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="username" label="提交用户" width="120"></el-table-column>
                <el-table-column prop="statusName" label="状态" width="100">
                    <template slot-scope="scope">
                        <el-tag :type="getStatusType(scope.row.status)" size="mini">
                            {{ scope.row.statusName || scope.row.status }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="priorityName" label="优先级" width="80">
                    <template slot-scope="scope">
                        <span :class="getPriorityClass(scope.row.priority)">
                            {{ scope.row.priorityName || scope.row.priority }}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column prop="assignedToName" label="处理人" width="120">
                    <template slot-scope="scope">
                        <span v-if="scope.row.assignedToName">{{ scope.row.assignedToName }}</span>
                        <span v-else style="color: #999;">未分配</span>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="提交时间" width="160">
                    <template slot-scope="scope">
                        {{ formatTime(scope.row.createTime) }}
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="200" fixed="right">
                    <template slot-scope="scope">
                        <el-button-group size="mini">
                            <el-button
                                type="primary"
                                @click.stop="handleFeedback(scope.row)"
                                :disabled="scope.row.status === 'RESOLVED' || scope.row.status === 'CLOSED'">
                                <i class="el-icon-edit"></i>
                                处理
                            </el-button>
                            <el-button
                                type="info"
                                @click.stop="assignFeedback(scope.row)"
                                :disabled="scope.row.status === 'RESOLVED' || scope.row.status === 'CLOSED'">
                                <i class="el-icon-user"></i>
                                分配
                            </el-button>
                            <el-button
                                type="danger"
                                @click.stop="deleteFeedback(scope.row)">
                                <i class="el-icon-delete"></i>
                                删除
                            </el-button>
                        </el-button-group>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div v-if="total > 0" class="pagination">
                <el-pagination
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"
                    :current-page="searchForm.current"
                    :page-sizes="[10, 20, 50, 100]"
                    :page-size="searchForm.size"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="total">
                </el-pagination>
            </div>
        </div>

        <!-- 处理反馈对话框 -->
        <el-dialog
            title="处理反馈"
            :visible.sync="handleVisible"
            width="60%"
            :before-close="closeHandleDialog">
            <div v-if="currentFeedback" class="handle-form">
                <el-descriptions :column="2" border style="margin-bottom: 20px;">
                    <el-descriptions-item label="工单号">{{ currentFeedback.workOrderId }}</el-descriptions-item>
                    <el-descriptions-item label="反馈类型">{{ currentFeedback.feedbackTypeName }}</el-descriptions-item>
                    <el-descriptions-item label="提交用户">{{ currentFeedback.username }}</el-descriptions-item>
                    <el-descriptions-item label="提交时间">{{ formatTime(currentFeedback.createTime) }}</el-descriptions-item>
                </el-descriptions>

                <h3>反馈内容</h3>
                <div class="feedback-content">
                    <h4>{{ currentFeedback.title }}</h4>
                    <p>{{ currentFeedback.content }}</p>
                    <div v-if="currentFeedback.contactInfo" class="contact-info">
                        <i class="el-icon-phone"></i>
                        联系方式：{{ currentFeedback.contactInfo }}
                    </div>
                </div>

                <h3>处理反馈</h3>
                <el-form :model="handleForm" :rules="handleRules" ref="handleForm" label-width="100px">
                    <el-form-item label="处理状态" prop="status">
                        <el-radio-group v-model="handleForm.status">
                            <el-radio label="PROCESSING">处理中</el-radio>
                            <el-radio label="RESOLVED">已解决</el-radio>
                            <el-radio label="CLOSED">已关闭</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item label="回复内容" prop="adminReply">
                        <el-input
                            type="textarea"
                            v-model="handleForm.adminReply"
                            placeholder="请输入回复内容"
                            :rows="6"
                            maxlength="1000"
                            show-word-limit>
                        </el-input>
                    </el-form-item>
                </el-form>
            </div>
            <span slot="footer" class="dialog-footer">
                <el-button @click="handleVisible = false">取消</el-button>
                <el-button type="primary" @click="submitHandle" :loading="handleLoading">
                    确定处理
                </el-button>
            </span>
        </el-dialog>

        <!-- 分配反馈对话框 -->
        <el-dialog
            title="分配反馈"
            :visible.sync="assignVisible"
            width="400px">
            <el-form :model="assignForm" :rules="assignRules" ref="assignForm" label-width="80px">
                <el-form-item label="工单号">
                    <span>{{ currentFeedback ? currentFeedback.workOrderId : '' }}</span>
                </el-form-item>
                <el-form-item label="处理人" prop="assignedTo">
                    <el-select v-model="assignForm.assignedTo" placeholder="请选择处理人" style="width: 100%;">
                        <el-option
                            v-for="user in adminUsers"
                            :key="user.id"
                            :label="user.username"
                            :value="user.id">
                        </el-option>
                    </el-select>
                </el-form-item>
            </el-form>
            <span slot="footer" class="dialog-footer">
                <el-button @click="assignVisible = false">取消</el-button>
                <el-button type="primary" @click="submitAssign" :loading="assignLoading">
                    确定分配
                </el-button>
            </span>
        </el-dialog>
    </div>
</template>

<script>
export default {
    name: 'FeedbackManage',
    data() {
        return {
            loading: false,
            handleLoading: false,
            assignLoading: false,
            handleVisible: false,
            assignVisible: false,
            currentFeedback: null,
            feedbackList: [],
            total: 0,
            feedbackTypes: [],
            adminUsers: [],
            stats: {
                pending: 0,
                processing: 0,
                resolved: 0,
                total: 0
            },
            statusOptions: [
                { label: '待处理', value: 'PENDING' },
                { label: '处理中', value: 'PROCESSING' },
                { label: '已解决', value: 'RESOLVED' },
                { label: '已关闭', value: 'CLOSED' }
            ],
            searchForm: {
                current: 1,
                size: 20,
                workOrderId: '',
                feedbackType: '',
                status: '',
                priority: '',
                assignedTo: ''
            },
            handleForm: {
                status: 'PROCESSING',
                adminReply: ''
            },
            handleRules: {
                status: [
                    { required: true, message: '请选择处理状态', trigger: 'change' }
                ],
                adminReply: [
                    { required: true, message: '请输入回复内容', trigger: 'blur' },
                    { min: 5, max: 1000, message: '回复内容长度在5到1000个字符', trigger: 'blur' }
                ]
            },
            assignForm: {
                assignedTo: ''
            },
            assignRules: {
                assignedTo: [
                    { required: true, message: '请选择处理人', trigger: 'change' }
                ]
            }
        }
    },
    created() {
        this.initData();
    },
    methods: {
        async initData() {
            try {
                // 并行加载数据
                await Promise.all([
                    this.loadFeedbackTypes(),
                    this.loadAdminUsers(),
                    this.loadFeedbackList(),
                    this.loadStats()
                ]);
            } catch (error) {
                console.error('初始化数据失败:', error);
                this.$message.error('加载数据失败');
            }
        },

        async loadFeedbackTypes() {
            try {
                const response = await this.$axios.get('/feedback/feedbackTypes');
                this.feedbackTypes = response.data;
            } catch (error) {
                console.error('加载反馈类型失败:', error);
            }
        },

        async loadAdminUsers() {
            try {
                // 这里需要根据实际API获取管理员用户列表
                // const response = await this.$axios.post('/user/list', { role: 'ADMIN' });
                // this.adminUsers = response.data;

                // 临时数据，实际使用时需要替换
                this.adminUsers = [
                    { id: 1, username: '管理员1' },
                    { id: 2, username: '管理员2' },
                    { id: 3, username: '客服1' }
                ];
            } catch (error) {
                console.error('加载管理员用户失败:', error);
            }
        },

        async loadFeedbackList() {
            this.loading = true;
            try {
                const response = await this.$axios.post('/feedback/list', this.searchForm);
                this.feedbackList = response.data || [];
                this.total = response.total || 0;
            } catch (error) {
                console.error('加载反馈列表失败:', error);
                this.$message.error('加载反馈列表失败');
            } finally {
                this.loading = false;
            }
        },

        async loadStats() {
            try {
                // 分别查询各状态的数量
                const statuses = ['PENDING', 'PROCESSING', 'RESOLVED'];
                const promises = statuses.map(status =>
                    this.$axios.post('/feedback/list', {
                        ...this.searchForm,
                        status,
                        current: 1,
                        size: 1
                    })
                );

                const results = await Promise.all(promises);
                this.stats.pending = results[0].total || 0;
                this.stats.processing = results[1].total || 0;
                this.stats.resolved = results[2].total || 0;
                this.stats.total = this.stats.pending + this.stats.processing + this.stats.resolved;
            } catch (error) {
                console.error('加载统计数据失败:', error);
            }
        },

        searchFeedbacks() {
            this.searchForm.current = 1;
            this.loadFeedbackList();
        },

        resetSearch() {
            this.searchForm = {
                current: 1,
                size: 20,
                workOrderId: '',
                feedbackType: '',
                status: '',
                priority: '',
                assignedTo: ''
            };
            this.loadFeedbackList();
        },

        async refreshStats() {
            await this.loadStats();
            this.$message.success('统计数据已刷新');
        },

        handleSizeChange(size) {
            this.searchForm.size = size;
            this.searchForm.current = 1;
            this.loadFeedbackList();
        },

        handleCurrentChange(current) {
            this.searchForm.current = current;
            this.loadFeedbackList();
        },

        handleRowClick(row) {
            this.viewDetail(row);
        },

        handleFeedback(row) {
            this.currentFeedback = row;
            this.handleForm = {
                status: row.status === 'PENDING' ? 'PROCESSING' : row.status,
                adminReply: row.adminReply || ''
            };
            this.handleVisible = true;
        },

        submitHandle() {
            this.$refs.handleForm.validate(async (valid) => {
                if (valid) {
                    this.handleLoading = true;
                    try {
                        await this.$axios.put(`/feedback/handle/${this.currentFeedback.workOrderId}`, {
                            adminReply: this.handleForm.adminReply,
                            status: this.handleForm.status
                        });
                        this.$message.success('处理成功');
                        this.handleVisible = false;
                        this.loadFeedbackList();
                        this.loadStats();
                    } catch (error) {
                        console.error('处理反馈失败:', error);
                        this.$message.error('处理失败');
                    } finally {
                        this.handleLoading = false;
                    }
                }
            });
        },

        assignFeedback(row) {
            this.currentFeedback = row;
            this.assignForm = {
                assignedTo: row.assignedTo || ''
            };
            this.assignVisible = true;
        },

        submitAssign() {
            this.$refs.assignForm.validate(async (valid) => {
                if (valid) {
                    this.assignLoading = true;
                    try {
                        await this.$axios.put(`/feedback/assign/${this.currentFeedback.workOrderId}`, {
                            assignedTo: this.assignForm.assignedTo
                        });
                        this.$message.success('分配成功');
                        this.assignVisible = false;
                        this.loadFeedbackList();
                    } catch (error) {
                        console.error('分配反馈失败:', error);
                        this.$message.error('分配失败');
                    } finally {
                        this.assignLoading = false;
                    }
                }
            });
        },

        async deleteFeedback(row) {
            try {
                await this.$confirm('确定要删除这条反馈吗？', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                });

                await this.$axios.delete(`/feedback/${row.workOrderId}`);
                this.$message.success('删除成功');
                this.loadFeedbackList();
                this.loadStats();
            } catch (error) {
                if (error !== 'cancel') {
                    console.error('删除反馈失败:', error);
                    this.$message.error('删除失败');
                }
            }
        },

        viewDetail(row) {
            // 可以实现查看详情的逻辑
            console.log('查看详情:', row);
        },

        closeHandleDialog() {
            this.handleVisible = false;
            this.currentFeedback = null;
        },

        getStatusType(status) {
            const statusMap = {
                'PENDING': 'warning',
                'PROCESSING': 'primary',
                'RESOLVED': 'success',
                'CLOSED': 'info'
            };
            return statusMap[status] || 'info';
        },

        getPriorityClass(priority) {
            const priorityMap = {
                'HIGH': 'priority-high',
                'MEDIUM': 'priority-medium',
                'LOW': 'priority-low'
            };
            return priorityMap[priority] || 'priority-medium';
        },

        formatTime(time) {
            if (!time) return '';
            return new Date(time).toLocaleString('zh-CN');
        }
    }
}
</script>

<style scoped lang="scss">
.feedback-manage {
    padding: 20px;
    background: #f5f5f5;
    min-height: 100vh;

    .page-header {
        background: white;
        padding: 24px;
        border-radius: 8px;
        margin-bottom: 20px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

        h1 {
            font-size: 24px;
            color: #333;
            margin: 0 0 8px 0;

            i {
                margin-right: 8px;
                color: #409eff;
            }
        }

        p {
            color: #666;
            margin: 0;
            font-size: 14px;
        }
    }

    .stats-container {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: 20px;
        margin-bottom: 20px;

        .stat-card {
            background: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            display: flex;
            align-items: center;

            .stat-icon {
                width: 50px;
                height: 50px;
                border-radius: 8px;
                display: flex;
                align-items: center;
                justify-content: center;
                margin-right: 16px;

                i {
                    font-size: 24px;
                    color: white;
                }

                &.pending {
                    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
                }

                &.processing {
                    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
                }

                &.resolved {
                    background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
                }

                &.total {
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                }
            }

            .stat-content {
                .stat-number {
                    font-size: 28px;
                    font-weight: bold;
                    color: #333;
                    line-height: 1;
                }

                .stat-label {
                    font-size: 14px;
                    color: #666;
                    margin-top: 4px;
                }
            }
        }
    }

    .filter-container {
        background: white;
        padding: 20px;
        border-radius: 8px;
        margin-bottom: 20px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

        .search-form {
            .el-form-item {
                margin-bottom: 0;
            }
        }
    }

    .feedback-list {
        background: white;
        border-radius: 8px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        overflow: hidden;

        .loading-container {
            padding: 40px;
        }

        .priority-high {
            color: #f56c6c;
            font-weight: bold;
        }

        .priority-medium {
            color: #e6a23c;
            font-weight: bold;
        }

        .priority-low {
            color: #67c23a;
            font-weight: bold;
        }

        .pagination {
            padding: 20px;
            text-align: right;
            border-top: 1px solid #ebeef5;
        }
    }
}

.handle-form {
    .feedback-content {
        background: #f8f9fa;
        padding: 20px;
        border-radius: 8px;
        margin-bottom: 20px;

        h4 {
            margin: 0 0 10px 0;
            color: #333;
        }

        p {
            margin: 0 0 10px 0;
            line-height: 1.6;
            color: #666;
        }

        .contact-info {
            color: #409eff;
            font-size: 14px;

            i {
                margin-right: 5px;
            }
        }
    }

    h3 {
        color: #333;
        margin-bottom: 15px;
        padding-bottom: 8px;
        border-bottom: 2px solid #409eff;
    }
}

// 响应式设计
@media (max-width: 768px) {
    .feedback-manage {
        padding: 10px;

        .stats-container {
            grid-template-columns: repeat(2, 1fr);
            gap: 10px;

            .stat-card {
                padding: 15px;

                .stat-icon {
                    width: 40px;
                    height: 40px;
                    margin-right: 12px;

                    i {
                        font-size: 20px;
                    }
                }

                .stat-content {
                    .stat-number {
                        font-size: 24px;
                    }

                    .stat-label {
                        font-size: 12px;
                    }
                }
            }
        }

        .filter-container {
            padding: 15px;
        }
    }
}
</style>