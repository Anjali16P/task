import { formatDateTime } from '../utils/format';

export default function FeedbackDetails({ feedback, onClose, onEdit }) {
  if (!feedback) return null;

  return (
    <div className="modal-backdrop" onClick={onClose} role="presentation">
      <div className="modal" onClick={(event) => event.stopPropagation()} role="dialog" aria-modal="true">
        <div className="modal-header">
          <h2>Feedback Details</h2>
          <button type="button" className="modal-close" onClick={onClose} aria-label="Close">
            &times;
          </button>
        </div>
        <div className="modal-body">
          <dl className="details-list">
            <dt>ID</dt>
            <dd>{feedback.id}</dd>
            <dt>Submitted By</dt>
            <dd>{feedback.username || `User #${feedback.userId}`}</dd>
            <dt>Created</dt>
            <dd>{formatDateTime(feedback.createdAt)}</dd>
            <dt>Last Updated</dt>
            <dd>{formatDateTime(feedback.updatedAt)}</dd>
            <dt>Feedback</dt>
            <dd className="details-message">{feedback.message}</dd>
          </dl>
        </div>
        <div className="modal-footer">
          <button type="button" className="btn btn-secondary" onClick={() => onEdit(feedback)}>
            Edit
          </button>
          <button type="button" className="btn btn-outline" onClick={onClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
