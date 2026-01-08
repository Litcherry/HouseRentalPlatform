<template>
    <div class="container-house-news">
        <div class="container-detail">
            //1.标题
            <div class="title">
                {{ houseNews.title }}
            </div>
            //2.发布时间与收藏情况
            <div class="create-time">
                <div class="time">
                    发布于{{ houseNews.createTime }}
                </div>
                <div class="save" @click="collectHouseOperation">
                    <i :class="saveList.length > 0 ? 'el-icon-star-on' : 'el-icon-star-off'"></i>
                    {{ saveList.length > 0 ? '取消收藏' : '收藏' }}
                </div>
            </div>
            //3.summary
            <div class="summary">
                {{ houseNews.summary }}
            </div>
            //4.资讯内容
            <div class="content" v-html="houseNews.content"></div>
            //5.评论
            <div class="evaluations">
                <Evaluations :userId="Number(userId)" :avatar="String(avatar)" :contentId="Number(houseNewsId)"
                    contentType="HOUSE_NEWS_INFO" />
            </div>
        </div>
    </div>
</template>

<script>
import Evaluations from "@/components/Evaluations"
export default {
    components: { Evaluations },
    data() {
        return {
            houseNewsId: null, // 当前资讯ID
            houseNews: {},     // 资讯详情对象（标题、内容、时间等）
            userId: null,      // 当前用户信息
            avatar: '',        // 当前用户信息
            saveList: [],      // 当前用户对该资讯的收藏状态
            startTime: 0,      // 页面停留计时起点
            hasRecorded: false, // 防止多次记录停留时间

        }
    },
    created() {
        this.getPathId(); //获取路由参数并加载资讯
        this.fetchUserInfo(); //获取当前登录用户信息
    },
    // 追踪用户页面停留时长
    mounted() {
        this.startTime = performance.now(); // 记录页面加载完成的时间（高精度时间戳、适合用于性能监控（如页面加载时间、渲染时间）
        window.addEventListener('beforeunload', this.handlePageLeave); // 监听页面即将离开事件
    },
    // 移除事件监听器，防止内存泄漏
    beforeDestroy() {
        window.removeEventListener('beforeunload', this.handlePageLeave);
    },
    methods: {
        // 停留时间相关
        handlePageLeave() {
            if (this.hasRecorded) return;

            const endTime = performance.now();
            const stayTime = Math.floor(endTime - this.startTime);

            this.hasRecorded = true;

            this.sendStayTime(stayTime);
        },
        // 将停留时间发送给后端统计
        async sendStayTime(duration) {
            try {
                await this.$axios.post('/flow-index/stayOperation', {
                    contentId: this.houseNewsId,
                    times: duration,
                    contentType: 'HOUSE_NEWS'
                });
            } catch (e) {
                console.error('记录停留时间失败:', e);
            }
        },
        // 获取路由 ID
        getPathId() {
            this.houseNewsId = this.$route.query.id;
            this.fetchHouseNewsInfo(this.houseNewsId);
            this.recordViewOperation(this.houseNewsId);
            this.recordSaveStatus(this.houseNewsId);
        },
        // 查询用户对于内容的收藏情况
        async recordSaveStatus(id) {
            try {
                const flowIndexQueryDto = {
                    contentId: id,
                    contentType: 'HOUSE_NEWS',
                    type: 2
                }
                const { data } = await this.$axios.post(`/flow-index/listUser`, flowIndexQueryDto);
                this.saveList = data;
            } catch (error) {
                console.info(error);
            }
        },
        // 浏览操作
        async recordViewOperation(id) {
            try {
                const flowIndex = {
                    contentId: id,
                    contentType: 'HOUSE_NEWS'
                }
                await this.$axios.post(`/flow-index/viewOperation`, flowIndex);
            } catch (error) {
                console.info(error);
            }
        },
        async fetchUserInfo() {
            try {
                const { data } = await this.$axios.get(`/user/auth`);
                this.userId = data.id;
                this.avatar = data.avatar;
            } catch (error) {
                this.$message.error('获取用户信息失败');
                console.error(error);
            }
        },
        async fetchHouseNewsInfo(id) {
            try {
                const { data } = await this.$axios.get(`/house-news/getById/${id}`);
                this.houseNews = { ...data };
            } catch (error) {
                this.$message.error('获取房屋资讯信息失败');
                console.error(error);
            }
        },
        // 收藏操作
        async collectHouseOperation() {
            try {
                const flowIndex = {
                    contentId: this.houseNewsId,
                    contentType: 'HOUSE_NEWS'
                }
                const { message } = await this.$axios.post(`/flow-index/saveOperation`, flowIndex);
                this.$notify.success({
                    title: '收藏操作',
                    message: message,
                    position: 'buttom-right',
                    duration: 1000,
                });
                this.recordSaveStatus(this.houseNewsId);
            } catch (error) {
                this.$message.error(error);
                console.info(error);
            }
        }
    }
}
</script>

<style scoped lang="scss">
.container-house-news {
    width: 100%;
    min-height: 100vh;
    background-color: rgb(244, 244, 244);

    .container-detail {
        width: 700px;
        min-height: 100vh;
        margin: 0 auto;
        background-color: rgb(255, 255, 255);
        box-shadow: 0 4px 8px rgb(220, 220, 220);
        padding: 20px 30px;
        box-sizing: border-box;

        .title {
            font-size: 32px;
            font-weight: 800;
        }

        .create-time {
            font-size: 14px;
            margin-block: 20px;
            display: flex;
            gap: 10px;
            align-items: center;

            .save {
                background-color: rgb(240, 240, 240);
                padding: 4px 10px;
                border-radius: 5px;
                cursor: pointer;

                &:hover {
                    background-color: rgb(234, 234, 234);
                }
            }
        }

        .summary {
            background-color: rgb(244, 246, 248);
            padding: 10px;
            font-size: 14px;
            line-height: 1.5;
        }
    }
}
</style>