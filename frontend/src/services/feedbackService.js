import api from './api';

export const feedbackService = {
  async getMyFeedback() {
    const response = await api.get('/feedback/my');
    if (response.status === 204) return null;
    return response.data;
  },

  async createFeedback(message) {
    const { data } = await api.post('/feedback', { message });
    return data;
  },

  async updateFeedback(id, message) {
    const { data } = await api.put(`/feedback/${id}`, { message });
    return data;
  },

  async adminGetAll() {
    const { data } = await api.get('/admin/feedback');
    return data;
  },

  async adminGetOne(id) {
    const { data } = await api.get(`/admin/feedback/${id}`);
    return data;
  },

  async adminCreate(userId, message) {
    const { data } = await api.post('/admin/feedback', { userId: Number(userId), message });
    return data;
  },

  async adminUpdate(id, message) {
    const { data } = await api.put(`/admin/feedback/${id}`, { message });
    return data;
  },

  async adminDelete(id) {
    const { data } = await api.delete(`/admin/feedback/${id}`);
    return data;
  },
};

export default feedbackService;
