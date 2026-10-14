import { useCallback, useEffect, useState } from 'react';
import Navbar from '../components/Navbar';
import FeedbackForm from '../components/FeedbackForm';
import Spinner from '../components/Spinner';
import feedbackService from '../services/feedbackService';
import { extractErrorMessage } from '../services/api';

export default function Feedback() {
  const [feedback, setFeedback] = useState(null);
  const [message, setMessage] = useState('');
  const [mode, setMode] = useState('view');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [notice, setNotice] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await feedbackService.getMyFeedback();
      setFeedback(data);
      setMessage(data ? data.message : '');
      setMode('view');
    } catch (err) {
      setNotice({ type: 'error', text: extractErrorMessage(err, 'Could not load your feedback.') });
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const handleAdd = () => {
    setNotice(null);
    setMessage('');
    setMode('add');
  };

  const handleEdit = () => {
    if (!feedback) return;
    setNotice(null);
    setMessage(feedback.message);
    setMode('edit');
  };

  const handleCancel = () => {
    setMode('view');
    setMessage(feedback ? feedback.message : '');
    setNotice(null);
  };

  const handleSubmit = async () => {
    const trimmed = message.trim();
    if (!trimmed) {
      setNotice({ type: 'error', text: 'Feedback cannot be empty.' });
      return;
    }
    setSaving(true);
    try {
      const isEdit = mode === 'edit' && feedback;
      const saved = isEdit
        ? await feedbackService.updateFeedback(feedback.id, trimmed)
        : await feedbackService.createFeedback(trimmed);
      setFeedback(saved);
      setMessage(saved.message);
      setMode('view');
      setNotice({
        type: 'success',
        text: isEdit ? 'Your feedback was updated successfully.' : 'Your feedback was submitted successfully.',
      });
    } catch (err) {
      setNotice({ type: 'error', text: extractErrorMessage(err, 'Could not save your feedback.') });
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="app-shell">
      <Navbar />
      <main className="page">
        {loading ? (
          <Spinner label="Loading your feedback..." />
        ) : (
          <FeedbackForm
            feedback={feedback}
            message={message}
            mode={mode}
            onMessageChange={setMessage}
            onAdd={handleAdd}
            onEdit={handleEdit}
            onCancel={handleCancel}
            onSubmit={handleSubmit}
            saving={saving}
            notice={notice}
          />
        )}
      </main>
    </div>
  );
}
