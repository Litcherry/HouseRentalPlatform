<template>
    <div class="feedback-submit">
        <div class="container">
            <div class="header">
                <h1>
                    <i class="el-icon-edit-outline"></i>
                    问题反馈
                </h1>
                <p>遇到问题或有好的建议？请告诉我们，我们会尽快处理您的反馈</p>
            </div>

            <div class="form-container">
                <el-form :model="feedbackForm" :rules="rules" ref="feedbackForm" label-width="120px">
                    <!-- 反馈类型 -->
                    <el-form-item label="反馈类型" prop="feedbackType">
                        <el-select v-model="feedbackForm.feedbackType" placeholder="请选择反馈类型" style="width: 100%;">
                            <el-option
                                v-for="item in feedbackTypes"
                                :key="item.value"
                                :label="item.label"
                                :value="item.value">
                            </el-option>
                        </el-select>
                    </el-form-item>

                    <!-- 反馈标题 -->
                    <el-form-item label="反馈标题" prop="title">
                        <el-input
                            v-model="feedbackForm.title"
                            placeholder="请简要描述您的问题或建议"
                            maxlength="100"
                            show-word-limit>
                        </el-input>
                    </el-form-item>

                    <!-- 关联房屋 -->
                    <el-form-item label="关联房屋">
                        <el-select
                            v-model="feedbackForm.houseId"
                            placeholder="可选，请选择相关房屋（如果反馈与具体房屋相关）"
                            clearable
                            filterable
                            style="width: 100%;">
                            <el-option
                                v-for="house in userHouses"
                                :key="house.id"
                                :label="house.name"
                                :value="house.id">
                            </el-option>
                        </el-select>
                    </el-form-item>

                    <!-- 关联订单 -->
                    <el-form-item label="关联订单">
                        <el-select
                            v-model="feedbackForm.orderId"
                            placeholder="可选，请选择相关订单（如果反馈与具体订单相关）"
                            clearable
                            filterable
                            style="width: 100%;">
                            <el-option
                                v-for="order in userOrders"
                                :key="order.id"
                                :label="`订单${order.id} - ${order.houseName}`"
                                :value="order.id">
                            </el-option>
                        </el-select>
                    </el-form-item>

                    <!-- 反馈内容 -->
                    <el-form-item label="反馈内容" prop="content">
                        <el-input
                            type="textarea"
                            v-model="feedbackForm.content"
                            placeholder="请详细描述您遇到的问题或建议，包括具体的操作步骤、预期结果等"
                            :rows="8"
                            maxlength="1000"
                            show-word-limit>
                        </el-input>
                    </el-form-item>

                    <!-- 联系方式 -->
                    <el-form-item label="联系方式">
                        <el-input
                            v-model="feedbackForm.contactInfo"
                            placeholder="可选，请留下您的联系方式（手机号或邮箱），方便我们与您联系">
                        </el-input>
                    </el-form-item>

                    <!-- 优先级 -->
                    <el-form-item label="优先级">
                        <el-radio-group v-model="feedbackForm.priority">
                            <el-radio label="LOW">低</el-radio>
                            <el-radio label="MEDIUM">中</el-radio>
                            <el-radio label="HIGH">高</el-radio>
                        </el-radio-group>
                    </el-form-item>

                    <!-- 提交按钮 -->
                    <el-form-item>
                        <el-button type="primary" @click="submitFeedback" :loading="submitting">
                            <i class="el-icon-s-promotion"></i>
                            提交反馈
                        </el-button>
                        <el-button @click="resetForm">
                            <i class="el-icon-refresh"></i>
                            重置
                        </el-button>
                        <el-button @click="goBack">
                            <i class="el-icon-back"></i>
                            返回
                        </el-button>
                    </el-form-item>
                </el-form>
            </div>

            <!-- 反馈须知 -->
            <div class="notice">
                <h3>
                    <i class="el-icon-info"></i>
                    反馈须知
                </h3>
                <ul>
                    <li>请详细描述您遇到的问题，这样我们可以更快地帮助您解决问题</li>
                    <li>如果问题与特定房屋或订单相关，请选择对应的信息</li>
                    <li>我们会尽快处理您的反馈，通常在1-3个工作日内给您回复</li>
                    <li>对于紧急问题，请将优先级设置为"高"</li>
                    <li>提交后您可以在"我的反馈"中查看处理进度</li>
                </ul>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'FeedbackSubmit',
    data() {
        return {
            submitting: false,
            feedbackForm: {
                feedbackType: '',
                title: '',
                content: '',
                contactInfo: '',
                priority: 'MEDIUM',
                houseId: null,
                orderId: null
            },
            feedbackTypes: [],
            userHouses: [],
            userOrders: [],
            rules: {
                feedbackType: [
                    { required: true, message: '请选择反馈类型', trigger: 'change' }
                ],
                title: [
                    { required: true, message: '请输入反馈标题', trigger: 'blur' },
                    { min: 5, max: 100, message: '标题长度在5到100个字符', trigger: 'blur' }
                ],
                content: [
                    { required: true, message: '请输入反馈内容', trigger: 'blur' },
                    { min: 10, max: 1000, message: '内容长度在10到1000个字符', trigger: 'blur' }
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
                // 加载反馈类型
                const typesResponse = await this.$axios.get('/feedback/feedbackTypes');
                this.feedbackTypes = typesResponse.data;

                // 加载用户房屋列表
                await this.loadUserHouses();

                // 加载用户订单列表
                await this.loadUserOrders();
            } catch (error) {
                console.error('初始化数据失败:', error);
                this.$message.error('加载页面数据失败');
            }
        },

        async loadUserHouses() {
            try {
                const response = await this.$axios.post('/landlord/listUser', {});
                if (response.data && response.data.length > 0) {
                    this.userHouses = response.data;
                }
            } catch (error) {
                console.error('加载用户房屋失败:', error);
            }
        },

        async loadUserOrders() {
            try {
                const response = await this.$axios.post('/house-order-info/listUser', {
                    current: 1,
                    size: 100
                });
                if (response.data && response.data.length > 0) {
                    this.userOrders = response.data;
                }
            } catch (error) {
                console.error('加载用户订单失败:', error);
            }
        },

        submitFeedback() {
            this.$refs.feedbackForm.validate(async (valid) => {
                if (valid) {
                    this.submitting = true;
                    try {
                        const response = await this.$axios.post('/feedback/save', this.feedbackForm);
                        this.$message.success('反馈提交成功！工单号：' + response.data);

                        // 清空表单
                        this.resetForm();

                        // 可选：跳转到我的反馈页面
                        setTimeout(() => {
                            this.$confirm('是否查看我的反馈列表？', '提交成功', {
                                confirmButtonText: '查看',
                                cancelButtonText: '继续提交',
                                type: 'success'
                            }).then(() => {
                                window.open('/feedback-my', '_blank');
                            }).catch(() => {
                                // 用户选择继续提交，不做任何操作
                            });
                        }, 1500);

                    } catch (error) {
                        console.error('提交反馈失败:', error);
                        this.$message.error('提交失败，请稍后重试');
                    } finally {
                        this.submitting = false;
                    }
                }
            });
        },

        resetForm() {
            this.$refs.feedbackForm.resetFields();
            this.feedbackForm = {
                feedbackType: '',
                title: '',
                content: '',
                contactInfo: '',
                priority: 'MEDIUM',
                houseId: null,
                orderId: null
            };
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
.feedback-submit {
    min-height: 100vh;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    padding: 20px;

    .container {
        max-width: 800px;
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

    .form-container {
        padding: 40px;

        .el-form {
            .el-form-item {
                margin-bottom: 30px;

                .el-select {
                    width: 100%;
                }

                .el-textarea {
                    .el-textarea__inner {
                        resize: vertical;
                    }
                }
            }
        }
    }

    .notice {
        background: #f8f9fa;
        border-left: 4px solid #667eea;
        padding: 20px;
        margin: 0 40px 40px;

        h3 {
            color: #333;
            margin-top: 0;
            margin-bottom: 15px;

            i {
                margin-right: 8px;
                color: #667eea;
            }
        }

        ul {
            margin: 0;
            padding-left: 20px;
            color: #666;
            line-height: 1.8;

            li {
                margin-bottom: 8px;

                &:last-child {
                    margin-bottom: 0;
                }
            }
        }
    }
}

// 响应式设计
@media (max-width: 768px) {
    .feedback-submit {
        padding: 10px;

        .container {
            .header {
                padding: 30px 20px;

                h1 {
                    font-size: 24px;
                }

                p {
                    font-size: 14px;
                }
            }

            .form-container {
                padding: 20px;
            }

            .notice {
                margin: 0 20px 20px;
                padding: 15px;

                ul {
                    font-size: 14px;
                }
            }
        }
    }
}
</style>