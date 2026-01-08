<template>
    <div class="landlord-application-container">
        <!-- 头部说明 -->
        <div class="header-section">
            <div class="header-content">
                <h1 class="page-title">
                    <i class="el-icon-house"></i>
                    申请成为房东
                </h1>
                <p class="page-description">完成实名认证，开启您的房东之旅</p>
            </div>
        </div>

        <!-- 申请表单 -->
        <div class="form-container">
            <el-card class="application-card" shadow="hover">
                <div slot="header" class="card-header">
                    <span class="card-title">
                        <i class="el-icon-user"></i>
                        身份认证信息
                    </span>
                    <span class="required-tip">* 为必填项</span>
                </div>

                <el-form :model="applicationForm" :rules="formRules" ref="applicationForm" label-width="120px" label-position="top">
                    <!-- 身份证上传区域 -->
                    <el-form-item label="身份证照片" prop="idcardImages" class="idcard-upload-section">
                        <div class="idcard-upload-container">
                            <!-- 正面照 -->
                            <div class="upload-item">
                                <div class="upload-label">
                                    <i class="el-icon-picture-outline"></i>
                                    身份证正面
                                    <span class="required">*</span>
                                </div>
                                <el-upload
                                    class="idcard-uploader"
                                    action="api/v1.0/house-rental-api/file/upload"
                                    :show-file-list="false"
                                    :on-success="handleFrontSuccess"
                                    :before-upload="beforeUpload"
                                    accept="image/*">
                                    <div class="upload-area" v-if="!idcardFront">
                                        <i class="el-icon-camera-solid upload-icon"></i>
                                        <p>点击上传身份证正面</p>
                                        <span class="upload-hint">支持 JPG/PNG 格式，大小不超过 5MB</span>
                                    </div>
                                    <div class="image-preview" v-else>
                                        <img :src="idcardFront" alt="身份证正面" class="idcard-image">
                                        <div class="image-overlay">
                                            <el-button type="text" icon="el-icon-zoom-in" @click="previewImage(idcardFront)">预览</el-button>
                                            <el-button type="text" icon="el-icon-delete" @click="removeFrontImage" class="delete-btn">删除</el-button>
                                        </div>
                                    </div>
                                </el-upload>
                            </div>

                            <!-- 反面照 -->
                            <div class="upload-item">
                                <div class="upload-label">
                                    <i class="el-icon-picture-outline"></i>
                                    身份证反面
                                    <span class="required">*</span>
                                </div>
                                <el-upload
                                    class="idcard-uploader"
                                    action="api/v1.0/house-rental-api/file/upload"
                                    :show-file-list="false"
                                    :on-success="handleOppositeSuccess"
                                    :before-upload="beforeUpload"
                                    accept="image/*">
                                    <div class="upload-area" v-if="!idcardOpposite">
                                        <i class="el-icon-camera-solid upload-icon"></i>
                                        <p>点击上传身份证反面</p>
                                        <span class="upload-hint">支持 JPG/PNG 格式，大小不超过 5MB</span>
                                    </div>
                                    <div class="image-preview" v-else>
                                        <img :src="idcardOpposite" alt="身份证反面" class="idcard-image">
                                        <div class="image-overlay">
                                            <el-button type="text" icon="el-icon-zoom-in" @click="previewImage(idcardOpposite)">预览</el-button>
                                            <el-button type="text" icon="el-icon-delete" @click="removeOppositeImage" class="delete-btn">删除</el-button>
                                        </div>
                                    </div>
                                </el-upload>
                            </div>
                        </div>
                    </el-form-item>

                    <!-- 身份证号输入 -->
                    <el-form-item label="身份证号码" prop="idcard">
                        <el-input
                            v-model="idcard"
                            placeholder="请输入18位身份证号码"
                            maxlength="18"
                            clearable
                            show-word-limit>
                            <i slot="prefix" class="el-icon-postcard"></i>
                        </el-input>
                        <div class="input-tip">
                            <i class="el-icon-info"></i>
                            请确保身份证号码清晰可辨，与上传的证件照片一致
                        </div>
                    </el-form-item>

                    <!-- 提交按钮 -->
                    <div class="submit-section">
                        <el-button
                            type="primary"
                            size="large"
                            :loading="submitting"
                            @click="submitApplication"
                            class="submit-btn">
                            <i class="el-icon-check"></i>
                            提交申请
                        </el-button>
                        <el-button size="large" @click="resetForm" class="reset-btn">
                            <i class="el-icon-refresh"></i>
                            重置表单
                        </el-button>
                    </div>
                </el-form>
            </el-card>

            <!-- 注意事项 -->
            <el-card class="notice-card" shadow="never">
                <div slot="header">
                    <span class="notice-title">
                        <i class="el-icon-warning-outline"></i>
                        注意事项
                    </span>
                </div>
                <div class="notice-content">
                    <ul>
                        <li><i class="el-icon-check check-icon"></i>请确保上传的身份证照片清晰、完整，无遮挡、无反光</li>
                        <li><i class="el-icon-check check-icon"></i>身份证信息必须真实有效，如提供虚假信息将被永久封禁</li>
                        <li><i class="el-icon-check check-icon"></i>提交申请后，我们将在1-3个工作日内完成审核</li>
                        <li><i class="el-icon-check check-icon"></i>审核通过后，您即可发布房源信息</li>
                    </ul>
                </div>
            </el-card>
        </div>

        <!-- 图片预览对话框 -->
        <el-dialog title="图片预览" :visible.sync="previewVisible" width="50%" center>
            <img :src="previewImageSrc" style="width: 100%;" alt="预览图片">
        </el-dialog>
    </div>
</template>

<script>
export default {
    data() {
        return {
            // 表单数据
            applicationForm: {},

            // 身份证相关
            idcardFront: null, // 身份证正面照
            idcardOpposite: null, // 身份证反面照
            idcard: null,

            // 状态控制
            submitting: false,
            previewVisible: false,
            previewImageSrc: '',

            // 表单验证规则
            formRules: {
                idcard: [
                    { required: true, message: '请输入身份证号码', trigger: 'blur' },
                    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号码格式', trigger: 'blur' }
                ]
            }
        }
    },
    created() {
        // 页面初始化
    },
    methods: {
        // 正面照上传成功
        handleFrontSuccess(res, file) {
            this.$notify({
                title: '身份证正面照上传',
                type: res.code === 200 ? 'success' : 'error',
                message: res.code === 200 ? '上传成功' : res.data || '上传失败',
                position: 'bottom-right',
                duration: 2000,
            })
            if (res.code === 200) {
                this.idcardFront = res.data;
            }
        },

        // 反面照上传成功
        handleOppositeSuccess(res, file) {
            this.$notify({
                title: '身份证反面照上传',
                type: res.code === 200 ? 'success' : 'error',
                message: res.code === 200 ? '上传成功' : res.data || '上传失败',
                position: 'bottom-right',
                duration: 2000,
            })
            if (res.code === 200) {
                this.idcardOpposite = res.data;
            }
        },

        // 上传前校验
        beforeUpload(file) {
            const isImage = file.type.startsWith('image/');
            const isLt5M = file.size / 1024 / 1024 < 5;

            if (!isImage) {
                this.$message.error('只能上传图片文件!');
                return false;
            }
            if (!isLt5M) {
                this.$message.error('图片大小不能超过 5MB!');
                return false;
            }
            return true;
        },

        // 删除正面照
        removeFrontImage() {
            this.idcardFront = null;
        },

        // 删除反面照
        removeOppositeImage() {
            this.idcardOpposite = null;
        },

        // 预览图片
        previewImage(imageSrc) {
            this.previewImageSrc = imageSrc;
            this.previewVisible = true;
        },

        // 表单验证
        validateForm() {
            if (!this.idcardFront) {
                this.$message.error('请上传身份证正面照');
                return false;
            }
            if (!this.idcardOpposite) {
                this.$message.error('请上传身份证反面照');
                return false;
            }
            if (!this.idcard) {
                this.$message.error('请输入身份证号码');
                return false;
            }

            // 身份证号格式校验
            const idcardPattern = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
            if (!idcardPattern.test(this.idcard)) {
                this.$message.error('身份证号码格式不正确');
                return false;
            }

            return true;
        },

        // 提交申请
        async submitApplication() {
            if (!this.validateForm()) {
                return;
            }

            this.submitting = true;

            try {
                const landlord = {
                    idcardFront: this.idcardFront,
                    idcardOpposite: this.idcardOpposite,
                    idcard: this.idcard.toUpperCase(), // 统一转为大写
                };

                const { message } = await this.$axios.post('/landlord/save', landlord);
                this.$message.success(message);

                // 延迟跳转，让用户看到成功提示
                setTimeout(() => {
                    this.$router.push('/user');
                }, 1500);

            } catch (error) {
                this.$notify({
                    title: '提交失败',
                    type: 'error',
                    message: (error.response && error.response.data && error.response.data.message) || error.message || '提交申请失败，请重试',
                    position: 'bottom-right',
                    duration: 3000,
                });
                console.error("提交房东申请异常：", error);
            } finally {
                this.submitting = false;
            }
        },

        // 重置表单
        resetForm() {
            this.$confirm('确定要重置所有填写的信息吗？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                this.idcardFront = null;
                this.idcardOpposite = null;
                this.idcard = null;
                this.$refs.applicationForm.resetFields();
                this.$message.success('表单已重置');
            }).catch(() => {
                // 用户取消重置
            });
        }
    }
}
</script>

<style scoped lang="scss">
.landlord-application-container {
    min-height: 100vh;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    padding: 20px;

    .header-section {
        text-align: center;
        margin-bottom: 40px;
        padding: 40px 0;

        .header-content {
            .page-title {
                font-size: 32px;
                color: #2c3e50;
                margin-bottom: 16px;
                font-weight: 600;

                i {
                    margin-right: 12px;
                    color: #409eff;
                }
            }

            .page-description {
                font-size: 16px;
                color: #606266;
                margin: 0;
                font-weight: 400;
            }
        }
    }

    .form-container {
        max-width: 900px;
        margin: 0 auto;

        .application-card {
            margin-bottom: 24px;
            border-radius: 12px;
            overflow: hidden;

            .card-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                background: linear-gradient(135deg, #409eff 0%, #36a3f7 100%);
                color: white;
                padding: 16px 24px;
                margin: -20px -20px 20px -20px;

                .card-title {
                    font-size: 18px;
                    font-weight: 600;

                    i {
                        margin-right: 8px;
                    }
                }

                .required-tip {
                    font-size: 14px;
                    opacity: 0.9;
                }
            }
        }

        .idcard-upload-section {
            .idcard-upload-container {
                display: flex;
                gap: 40px;
                flex-wrap: wrap;

                .upload-item {
                    flex: 1;
                    min-width: 300px;

                    .upload-label {
                        display: flex;
                        align-items: center;
                        font-weight: 600;
                        color: #303133;
                        margin-bottom: 16px;
                        font-size: 16px;

                        i {
                            margin-right: 8px;
                            color: #409eff;
                        }

                        .required {
                            color: #f56c6c;
                            margin-left: 4px;
                        }
                    }

                    .idcard-uploader {
                        width: 100%;

                        .upload-area {
                            border: 2px dashed #dcdfe6;
                            border-radius: 8px;
                            padding: 40px 20px;
                            text-align: center;
                            background-color: #fafafa;
                            transition: all 0.3s ease;
                            cursor: pointer;

                            &:hover {
                                border-color: #409eff;
                                background-color: #f0f8ff;
                            }

                            .upload-icon {
                                font-size: 48px;
                                color: #c0c4cc;
                                margin-bottom: 16px;
                            }

                            p {
                                margin: 0 0 8px 0;
                                color: #606266;
                                font-size: 16px;
                                font-weight: 500;
                            }

                            .upload-hint {
                                color: #909399;
                                font-size: 12px;
                            }
                        }

                        .image-preview {
                            position: relative;
                            width: 100%;
                            height: 200px;
                            border-radius: 8px;
                            overflow: hidden;
                            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

                            .idcard-image {
                                width: 100%;
                                height: 100%;
                                object-fit: cover;
                                transition: transform 0.3s ease;
                            }

                            .image-overlay {
                                position: absolute;
                                top: 0;
                                left: 0;
                                right: 0;
                                bottom: 0;
                                background: rgba(0, 0, 0, 0.6);
                                display: flex;
                                justify-content: center;
                                align-items: center;
                                opacity: 0;
                                transition: opacity 0.3s ease;

                                .el-button {
                                    margin: 0 8px;
                                    color: white;

                                    &:hover {
                                        transform: translateY(-2px);
                                    }

                                    &.delete-btn {
                                        color: #f56c6c;
                                    }
                                }
                            }

                            &:hover {
                                .image-overlay {
                                    opacity: 1;
                                }

                                .idcard-image {
                                    transform: scale(1.05);
                                }
                            }
                        }
                    }
                }
            }
        }

        .input-tip {
            display: flex;
            align-items: center;
            margin-top: 8px;
            padding: 8px 12px;
            background-color: #f4f4f5;
            border-radius: 4px;
            font-size: 12px;
            color: #909399;

            i {
                margin-right: 6px;
                color: #409eff;
            }
        }

        .submit-section {
            text-align: center;
            margin-top: 40px;
            padding-top: 20px;
            border-top: 1px solid #ebeef5;

            .submit-btn {
                padding: 14px 40px;
                font-size: 16px;
                font-weight: 600;
                border-radius: 8px;
                margin-right: 16px;
                box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);

                &:hover {
                    transform: translateY(-2px);
                    box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
                }

                i {
                    margin-right: 8px;
                }
            }

            .reset-btn {
                padding: 14px 32px;
                font-size: 16px;
                border-radius: 8px;

                &:hover {
                    transform: translateY(-2px);
                }

                i {
                    margin-right: 8px;
                }
            }
        }

        .notice-card {
            border-radius: 12px;
            border: 1px solid #e4e7ed;

            .notice-title {
                font-size: 16px;
                font-weight: 600;
                color: #e6a23c;

                i {
                    margin-right: 8px;
                }
            }

            .notice-content {
                ul {
                    list-style: none;
                    padding: 0;
                    margin: 0;

                    li {
                        display: flex;
                        align-items: center;
                        margin-bottom: 12px;
                        color: #606266;
                        font-size: 14px;
                        line-height: 1.6;

                        &:last-child {
                            margin-bottom: 0;
                        }

                        .check-icon {
                            color: #67c23a;
                            margin-right: 12px;
                            font-size: 16px;
                        }
                    }
                }
            }
        }
    }
}

/* 响应式设计 */
@media (max-width: 768px) {
    .landlord-application-container {
        padding: 16px;

        .header-section {
            padding: 20px 0;
            margin-bottom: 24px;

            .page-title {
                font-size: 24px !important;
            }

            .page-description {
                font-size: 14px !important;
            }
        }

        .form-container {
            .idcard-upload-container {
                flex-direction: column !important;
                gap: 24px !important;

                .upload-item {
                    min-width: 100% !important;
                }
            }

            .submit-section {
                .submit-btn, .reset-btn {
                    width: 100%;
                    margin: 8px 0;
                }
            }
        }
    }
}

/* 动画效果 */
@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(30px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.application-card, .notice-card {
    animation: fadeInUp 0.6s ease-out;
}

.notice-card {
    animation-delay: 0.2s;
}

/* Element UI 样式覆盖 */
::v-deep .el-form-item__label {
    font-weight: 600;
    color: #303133;
}

::v-deep .el-input__inner {
    border-radius: 6px;
    border: 1px solid #dcdfe6;
    transition: all 0.3s ease;

    &:focus {
        border-color: #409eff;
        box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
    }
}

::v-deep .el-card__header {
    border: none;
}
</style>