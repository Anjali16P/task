import { useCallback, useEffect, useState } from 'react';
import Navbar from '../components/Navbar';
import FeedbackTable from '../components/FeedbackTable';
import FeedbackDetails from '../components/FeedbackDetails';
import Alert from '../components/Alert';
import feedbackService from '../services/feedbackService';
import { extractErrorMessage } from '../services/api';

export default function AdminDashboard() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState(null);
  const [viewing, setViewing] = useState(null);
  const [editing, setEditing] = useState(null);
  const [adding, setAdding] = useState(null);
  const [busy, setBusy] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await feedbackService.adminGetAll();
      setItems(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(extractErrorMessage(err, 'Could not load feedback records.'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const handleView = (item) => {
    setNotice(null);
    setViewing(item);
  };

  const handleEdit = (item) => {
    setNotice(null);
    setViewing(null);
    setEditing({ id: item.id, message: item.message });
  };

  const handleDelete = async (item) => {
    if (!window.confirm(`Delete feedback #${item.id}? This action cannot be undone.`)) {
      return;
    }
    try {
      await feedbackService.adminDelete(item.id);
      setNotice({ type: 'success', text: `Feedback #${item.id} was deleted.` });
      await load();
    } catch (err) {
      setNotice({ type: 'error', text: extractErrorMessage(err, 'Could not delete feedback.') });
    }
  };

  const submitEdit = async () => {
    const trimmed = editing.message.trim();
    if (!trimmed) {
      setNotice({ type: 'error', text: 'Feedback cannot be empty.' });
      return;
    }
    setBusy(true);
    try {
      await feedbackService.adminUpdate(editing.id, trimmed);
      setEditing(null);
      setNotice({ type: 'success', text: 'Feedback updated successfully.' });
      await load();
    } catch (err) {
      setNotice({ type: 'error', text: extractErrorMessage(err, 'Could not update feedback.') });
    } finally {
      setBusy(false);
    }
  };

  const submitAdd = async () => {
    if (!String(adding.userId).trim()) {
      setNotice({ type: 'error', text: 'A user ID is required.' });
      return;
    }
    const trimmed = adding.message.trim();
    if (!trimmed) {
      setNotice({ type: 'error', text: 'Feedback cannot be empty.' });
      return;
    }
    setBusy(true);
    try {
      await feedbackService.adminCreate(adding.userId, trimmed);
      setAdding(null);
      setNotice({ type: 'success', text: 'Feedback created successfully.' });
      await load();
    } catch (err) {
      setNotice({ type: 'error', text: extractErrorMessage(err, 'Could not create feedback.') });
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="app-shell">
      <Navbar />
      <main className="page">
        <div className="admin-header">
          <div>
            <h1>Admin Feedback Dashboard</h1>
            <p className="admin-subtitle">
              {loading ? 'Loading...' : `${items.length} feedback record${items.length === 1 ? '' : 's'}`}
            </p>
          </div>
          <div className="admin-header-actions">
            <button type="button" className="btn btn-outline" onClick={load} disabled={loading}>
              Refresh
            </button>
            <button
              type="button"
              className="btn btn-primary"
              onClick={() => {
                setNotice(null);
                setAdding({ userId: '', message: '' });
              }}
            >
              Add Feedback
            </button>
          </div>
        </div>

        <Alert type={notice?.type || 'info'} message={notice?.text} onClose={() => setNotice(null)} />

        {error ? (
          <div className="card">
            <Alert type="error" message={error} />
            <button type="button" className="btn btn-secondary" onClick={load}>
              Try again
            </button>
          </div>
        ) : (
          <div className="card table-card">
            <FeedbackTable
              items={items}
              loading={loading}
              onView={handleView}
              onEdit={handleEdit}
              onDelete={handleDelete}
            />
          </div>
        )}
      </main>

      <FeedbackDetails feedback={viewing} onClose={() => setViewing(null)} onEdit={handleEdit} />

      {editing && (
        <div className="modal-backdrop" onClick={() => setEditing(null)} role="presentation">
          <div className="modal" onClick={(event) => event.stopPropagation()} role="dialog" aria-modal="true">
            <div className="modal-header">
              <h2>Edit Feedback #{editing.id}</h2>
              <button type="button" className="modal-close" onClick={() => setEditing(null)} aria-label="Close">
                &times;
              </button>
            </div>
            <div className="modal-body">
              <label htmlFor="edit-message" className="field-label">
                Feedback
              </label>
              <textarea
                id="edit-message"
                className="feedback-textarea modal-textarea"
                rows={7}
                maxLength={2000}
                value={editing.message}
                onChange={(event) => setEditing((previous) => ({ ...previous, message: event.target.value }))}
              />
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-outline" onClick={() => setEditing(null)} disabled={busy}>
                Cancel
              </button>
              <button type="button" className="btn btn-primary" onClick={submitEdit} disabled={busy}>
                {busy ? 'Saving...' : 'Save Changes'}
              </button>
            </div>
          </div>
        </div>
      )}

      {adding && (
        <div className="modal-backdrop" onClick={() => setAdding(null)} role="presentation">
          <div className="modal" onClick={(event) => event.stopPropagation()} role="dialog" aria-modal="true">
            <div className="modal-header">
              <h2>Add Feedback</h2>
              <button type="button" className="modal-close" onClick={() => setAdding(null)} aria-label="Close">
                &times;
              </button>
            </div>
            <div className="modal-body">
              <div className="form-group">
                <label htmlFor="add-user-id" className="field-label">
                  User ID
                </label>
                <input
                  id="add-user-id"
                  className="form-input"
                  type="number"
                  min="1"
                  value={adding.userId}
                  onChange={(event) => setAdding((previous) => ({ ...previous, userId: event.target.value }))}
                  placeholder="e.g. 2"
                />
              </div>
              <div className="form-group">
                <label htmlFor="add-message" className="field-label">
                  Feedback
                </label>
                <textarea
                  id="add-message"
                  className="feedback-textarea modal-textarea"
                  rows={6}
                  maxLength={2000}
                  value={adding.message}
                  onChange={(event) => setAdding((previous) => ({ ...previous, message: event.target.value }))}
                />
              </div>
              <p className="modal-hint">Each user may have only one feedback entry.</p>
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-outline" onClick={() => setAdding(null)} disabled={busy}>
                Cancel
              </button>
              <button type="button" className="btn btn-primary" onClick={submitAdd} disabled={busy}>
                {busy ? 'Creating...' : 'Create'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
