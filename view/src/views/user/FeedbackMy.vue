<template>
    <div class="feedback-my">
        <div class="container">
            <div class="header">
                <h1>
                    <i class="el-icon-view"></i>
                    我的反馈
                </h1>
                <p>查看您提交的所有问题反馈及其处理进度</p>
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
                    <el-form-item>
                        <el-button type="primary" @click="searchFeedbacks">
                            <i class="el-icon-search"></i>
                            搜索
                        </el-button>
                        <el-button @click="resetSearch">
                            <i class="el-icon-refresh"></i>
                            重置
                        </el-button>
                    </el-form-item>
                </el-form>
            </div>

            <!-- 反馈列表 -->
            <div class="feedback-list">
                <div v-if="loading" class="loading-container">
                    <el-skeleton :rows="5" animated />
                </div>

                <div v-else-if="feedbackList.length === 0" class="empty-container">
                    <el-empty description="暂无反馈记录">
                        <el-button type="primary" @click="goToSubmit">
                            <i class="el-icon-edit-outline"></i>
                            立即反馈
                        </el-button>
                    </el-empty>
                </div>

                <div v-else class="feedback-items">
                    <div
                        v-for="item in feedbackList"
                        :key="item.workOrderId"
                        class="feedback-item"
                        @click="viewDetail(item)">
                        <div class="item-header">
                            <div class="work-order">
                                <span class="label">工单号：</span>
                                <span class="value">{{ item.workOrderId }}</span>
                            </div>
                            <div class="status">
                                <el-tag :type="getStatusType(item.status)" size="small">
                                    {{ item.statusName || item.status }}
                                </el-tag>
                            </div>
                        </div>

                        <div class="item-content">
                            <h3 class="title">{{ item.title }}</h3>
                            <div class="meta">
                                <span class="type">
                                    <i class="el-icon-collection-tag"></i>
                                    {{ item.feedbackTypeName || item.feedbackType }}
                                </span>
                                <span class="priority" :class="getPriorityClass(item.priority)">
                                    <i class="el-icon-warning-outline"></i>
                                    {{ item.priorityName || item.priority }}
                                </span>
                                <span class="time">
                                    <i class="el-icon-time"></i>
                                    {{ formatTime(item.createTime) }}
                                </span>
                            </div>
                            <p class="content">{{ truncateText(item.content, 150) }}</p>
                        </div>

                        <div class="item-footer" v-if="item.adminReply">
                            <div class="reply-info">
                                <i class="el-icon-chat-line-square"></i>
                                <span>已回复</span>
                                <span class="reply-time">{{ formatTime(item.replyTime) }}</span>
                            </div>
                        </div>

                        <div class="house-info" v-if="item.houseTitle">
                            <i class="el-icon-house"></i>
                            <span>相关房屋：{{ item.houseTitle }}</span>
                        </div>
                    </div>
                </div>

                <!-- 分页 -->
                <div v-if="total > 0" class="pagination">
                    <el-pagination
                        @size-change="handleSizeChange"
                        @current-change="handleCurrentChange"
                        :current-page="searchForm.current"
                        :page-sizes="[10, 20, 50]"
                        :page-size="searchForm.size"
                        layout="total, sizes, prev, pager, next, jumper"
                        :total="total">
                    </el-pagination>
                </div>
            </div>

            <!-- 底部操作 -->
            <div class="footer">
                <el-button type="primary" @click="goToSubmit">
                    <i class="el-icon-plus"></i>
                    新建反馈
                </el-button>
                <el-button @click="goBack">
                    <i class="el-icon-back"></i>
                    返回
                </el-button>
            </div>
        </div>

        <!-- 详情对话框 -->
        <el-dialog
            title="反馈详情"
            :visible.sync="detailVisible"
            width="70%"
            :before-close="closeDetail">
            <div v-if="currentFeedback" class="feedback-detail">
                <div class="detail-section">
                    <h3>基本信息</h3>
                    <el-descriptions :column="2" border>
                        <el-descriptions-item label="工单号">
                            {{ currentFeedback.workOrderId }}
                        </el-descriptions-item>
                        <el-descriptions-item label="反馈类型">
                            {{ currentFeedback.feedbackTypeName || currentFeedback.feedbackType }}
                        </el-descriptions-item>
                        <el-descriptions-item label="处理状态">
                            <el-tag :type="getStatusType(currentFeedback.status)">
                                {{ currentFeedback.statusName || currentFeedback.status }}
                            </el-tag>
                        </el-descriptions-item>
                        <el-descriptions-item label="优先级">
                            <span :class="getPriorityClass(currentFeedback.priority)">
                                {{ currentFeedback.priorityName || currentFeedback.priority }}
                            </span>
                        </el-descriptions-item>
                        <el-descriptions-item label="提交时间">
                            {{ formatTime(currentFeedback.createTime) }}
                        </el-descriptions-item>
                        <el-descriptions-item label="相关房屋" v-if="currentFeedback.houseTitle">
                            {{ currentFeedback.houseTitle }}
                        </el-descriptions-item>
                    </el-descriptions>
                </div>

                <div class="detail-section">
                    <h3>反馈内容</h3>
                    <div class="content-detail">
                        <h4>{{ currentFeedback.title }}</h4>
                        <p>{{ currentFeedback.content }}</p>
                        <div v-if="currentFeedback.contactInfo" class="contact-info">
                            <i class="el-icon-phone"></i>
                            联系方式：{{ currentFeedback.contactInfo }}
                        </div>
                    </div>
                </div>

                <div class="detail-section" v-if="currentFeedback.adminReply">
                    <h3>管理员回复</h3>
                    <div class="reply-detail">
                        <div class="reply-time">
                            回复时间：{{ formatTime(currentFeedback.replyTime) }}
                        </div>
                        <div class="reply-content">
                            {{ currentFeedback.adminReply }}
                        </div>
                    </div>
                </div>
            </div>
        </el-dialog>
    </div>
</template>

<script>
export default {
    name: 'FeedbackMy',
    data() {
        return {
            loading: false,
            detailVisible: false,
            currentFeedback: null,
            feedbackList: [],
            total: 0,
            feedbackTypes: [],
            statusOptions: [
                { label: '待处理', value: 'PENDING' },
                { label: '处理中', value: 'PROCESSING' },
                { label: '已解决', value: 'RESOLVED' },
                { label: '已关闭', value: 'CLOSED' }
            ],
            searchForm: {
                current: 1,
                size: 10,
                workOrderId: '',
                feedbackType: '',
                status: ''
            }
        }
    },
    created() {
        this.initData();
    },
    methods: {
        async initData() {
            try {
                // 加载反馈类型
                const typesResponse = await this.$axios.get('/feedback/feedbackTypes');
                this.feedbackTypes = typesResponse.data;

                // 加载反馈列表
                await this.loadFeedbackList();
            } catch (error) {
                console.error('初始化数据失败:', error);
                this.$message.error('加载数据失败');
            }
        },

        async loadFeedbackList() {
            this.loading = true;
            try {
                const response = await this.$axios.post('/feedback/listUser', this.searchForm);
                this.feedbackList = response.data || [];
                this.total = response.total || 0;
            } catch (error) {
                console.error('加载反馈列表失败:', error);
                this.$message.error('加载反馈列表失败');
            } finally {
                this.loading = false;
            }
        },

        searchFeedbacks() {
            this.searchForm.current = 1;
            this.loadFeedbackList();
        },

        resetSearch() {
            this.searchForm = {
                current: 1,
                size: 10,
                workOrderId: '',
                feedbackType: '',
                status: ''
            };
            this.loadFeedbackList();
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

        viewDetail(item) {
            this.currentFeedback = item;
            this.detailVisible = true;
        },

        closeDetail() {
            this.detailVisible = false;
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
        },

        truncateText(text, length) {
            if (!text) return '';
            return text.length > length ? text.substring(0, length) + '...' : text;
        },

        goToSubmit() {
            window.open('/feedback-submit', '_blank');
        },

        goBack() {
            if (window.opener) {
                window.close();
            } else {
                this.$router.go(-1);
            }
        }
    }
}
</script>

<style scoped lang="scss">
.feedback-my {
    min-height: 100vh;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    padding: 20px;

    .container {
        max-width: 1200px;
        margin: 0 auto;
        background: white;
        border-radius: 10px;
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
        overflow: hidden;
    }

    .header {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        padding: 40px;
        text-align: center;

        h1 {
            font-size: 32px;
            margin-bottom: 10px;

            i {
                margin-right: 10px;
            }
        }

        p {
            font-size: 16px;
            opacity: 0.9;
            margin: 0;
        }
    }

    .filter-container {
        padding: 30px 40px 20px;
        background: #f8f9fa;
        border-bottom: 1px solid #e9ecef;

        .search-form {
            .el-form-item {
                margin-bottom: 0;
            }
        }
    }

    .feedback-list {
        padding: 20px 40px;

        .loading-container,
        .empty-container {
            padding: 50px 0;
        }

        .feedback-items {
            .feedback-item {
                border: 1px solid #e9ecef;
                border-radius: 8px;
                padding: 20px;
                margin-bottom: 15px;
                cursor: pointer;
                transition: all 0.3s ease;

                &:hover {
                    border-color: #667eea;
                    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
                }

                .item-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 15px;

                    .work-order {
                        .label {
                            color: #666;
                            font-size: 14px;
                        }

                        .value {
                            color: #333;
                            font-weight: 600;
                            font-family: monospace;
                        }
                    }
                }

                .item-content {
                    .title {
                        font-size: 18px;
                        color: #333;
                        margin: 0 0 10px 0;
                        line-height: 1.4;
                    }

                    .meta {
                        display: flex;
                        gap: 20px;
                        margin-bottom: 12px;
                        font-size: 13px;
                        color: #666;

                        span {
                            display: flex;
                            align-items: center;
                            gap: 4px;

                            i {
                                font-size: 14px;
                            }
                        }

                        .priority {
                            &.priority-high {
                                color: #f56c6c;
                            }

                            &.priority-medium {
                                color: #e6a23c;
                            }

                            &.priority-low {
                                color: #67c23a;
                            }
                        }
                    }

                    .content {
                        color: #666;
                        line-height: 1.6;
                        margin: 0;
                    }
                }

                .item-footer {
                    margin-top: 15px;
                    padding-top: 15px;
                    border-top: 1px solid #f0f0f0;

                    .reply-info {
                        display: flex;
                        align-items: center;
                        gap: 8px;
                        color: #67c23a;
                        font-size: 14px;

                        .reply-time {
                            color: #999;
                            font-size: 12px;
                        }
                    }
                }

                .house-info {
                    margin-top: 10px;
                    color: #409eff;
                    font-size: 13px;
                    display: flex;
                    align-items: center;
                    gap: 5px;
                }
            }
        }

        .pagination {
            margin-top: 30px;
            display: flex;
            justify-content: center;
        }
    }

    .footer {
        padding: 20px 40px;
        text-align: center;
        border-top: 1px solid #e9ecef;
        background: #f8f9fa;

        .el-button {
            margin: 0 10px;
        }
    }
}

.feedback-detail {
    .detail-section {
        margin-bottom: 30px;

        h3 {
            color: #333;
            margin-bottom: 15px;
            padding-bottom: 8px;
            border-bottom: 2px solid #667eea;
        }

        .content-detail {
            h4 {
                color: #333;
                margin: 0 0 10px 0;
            }

            p {
                color: #666;
                line-height: 1.8;
                margin: 0 0 15px 0;
            }

            .contact-info {
                color: #409eff;
                font-size: 14px;
            }
        }

        .reply-detail {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 8px;

            .reply-time {
                color: #666;
                font-size: 14px;
                margin-bottom: 10px;
            }

            .reply-content {
                color: #333;
                line-height: 1.8;
            }
        }
    }
}

// 响应式设计
@media (max-width: 768px) {
    .feedback-my {
        padding: 10px;

        .container {
            .header {
                padding: 30px 20px;

                h1 {
                    font-size: 24px;
                }
            }

            .filter-container {
                padding: 20px;
            }

            .feedback-list {
                padding: 20px;

                .feedback-items {
                    .feedback-item {
                        padding: 15px;

                        .item-header {
                            flex-direction: column;
                            align-items: flex-start;
                            gap: 10px;
                        }

                        .item-content {
                            .meta {
                                flex-wrap: wrap;
                                gap: 10px;
                            }
                        }
                    }
                }
            }
        }
    }
}
</style>