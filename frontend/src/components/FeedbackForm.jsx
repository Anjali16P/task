import Alert from './Alert';
import { formatDateTime } from '../utils/format';

const MAX_LENGTH = 2000;

export default function FeedbackForm({
  feedback,
  message,
  mode,
  onMessageChange,
  onAdd,
  onEdit,
  onCancel,
  onSubmit,
  saving,
  notice,
}) {
  const editing = mode === 'add' || mode === 'edit';
  const hasFeedback = Boolean(feedback);

  const handleSubmit = (event) => {
    event.preventDefault();
    onSubmit();
  };

  return (
    <div className="card feedback-card">
      <div className="feedback-header">
        <h1>Feedback</h1>
        <div className="feedback-actions">
          <button
            type="button"
            className="btn btn-primary"
            onClick={onAdd}
            disabled={hasFeedback || saving || mode === 'add'}
            title={hasFeedback ? 'You already have feedback. Use Edit Feedback instead.' : 'Add new feedback'}
          >
            Add Feedback
          </button>
          <button
            type="button"
            className="btn btn-secondary"
            onClick={onEdit}
            disabled={!hasFeedback || saving || mode === 'edit'}
            title={hasFeedback ? 'Edit your feedback' : 'You have no feedback to edit yet.'}
          >
            Edit Feedback
          </button>
        </div>
      </div>

      <Alert type={notice?.type || 'info'} message={notice?.text} onClose={notice?.onClose} />

      <form onSubmit={handleSubmit} noValidate>
        <label htmlFor="feedback-message" className="field-label">
          Your Feedback
        </label>
        <textarea
          id="feedback-message"
          className="feedback-textarea"
          value={message}
          onChange={(event) => onMessageChange(event.target.value)}
          maxLength={MAX_LENGTH}
          readOnly={!editing}
          rows={8}
          placeholder={
            editing ? 'Type your feedback here...' : 'Click Add Feedback or Edit Feedback to start.'
          }
        />
        <div className="feedback-meta-row">
          <span className="char-count">
            {message.length}/{MAX_LENGTH}
          </span>
          {feedback && (
            <span className="feedback-meta">
              Submitted {formatDateTime(feedback.createdAt)}
              {feedback.updatedAt && feedback.updatedAt !== feedback.createdAt
                ? ` · Updated ${formatDateTime(feedback.updatedAt)}`
                : ''}
            </span>
          )}
        </div>

        {editing ? (
          <div className="feedback-submit-row">
            <button type="submit" className="btn btn-primary" disabled={!message.trim() || saving}>
              {saving ? 'Saving...' : 'Submit'}
            </button>
            <button type="button" className="btn btn-outline" onClick={onCancel} disabled={saving}>
              Cancel
            </button>
          </div>
        ) : (
          <p className="feedback-hint">
            {hasFeedback
              ? 'Your feedback is shown above. Click Edit Feedback to make changes.'
              : 'You have not submitted any feedback yet. Click Add Feedback to create one.'}
          </p>
        )}
      </form>
    </div>
  );
}
