import { formatDateTime, truncate } from '../utils/format';

export default function FeedbackTable({ items, loading, onView, onEdit, onDelete }) {
  if (loading) {
    return (
      <div className="table-placeholder">
        <span className="spinner" aria-hidden="true" />
        <span>Loading feedback records...</span>
      </div>
    );
  }

  if (!items || items.length === 0) {
    return <div className="table-placeholder">No feedback has been submitted yet.</div>;
  }

  return (
    <div className="table-wrapper">
      <table className="data-table">
        <thead>
          <tr>
            <th style={{ width: '80px' }}>ID</th>
            <th>Feedback</th>
            <th style={{ width: '200px' }}>Date</th>
            <th style={{ width: '240px' }}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {items.map((item) => (
            <tr key={item.id}>
              <td>{item.id}</td>
              <td title={item.message}>{truncate(item.message, 90)}</td>
              <td>{formatDateTime(item.createdAt)}</td>
              <td>
                <div className="row-actions">
                  <button type="button" className="btn btn-outline btn-sm" onClick={() => onView(item)}>
                    View
                  </button>
                  <button type="button" className="btn btn-secondary btn-sm" onClick={() => onEdit(item)}>
                    Edit
                  </button>
                  <button type="button" className="btn btn-danger btn-sm" onClick={() => onDelete(item)}>
                    Delete
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
