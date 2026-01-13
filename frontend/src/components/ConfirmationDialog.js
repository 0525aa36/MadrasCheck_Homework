import React from 'react';

const ConfirmationDialog = ({ isOpen, onClose, onConfirm, title, message }) => {
  if (!isOpen) return null;

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(0, 0, 0, 0.5)',
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      zIndex: 1000
    }}>
      <div style={{
        backgroundColor: 'white',
        padding: '20px',
        borderRadius: '8px',
        boxShadow: '0 4px 10px rgba(0, 0, 0, 0.1)',
        maxWidth: '400px',
        width: '90%',
        textAlign: 'center'
      }}>
        <h3 style={{ marginTop: 0, marginBottom: '15px', color: '#333' }}>{title}</h3>
        <p style={{ marginBottom: '20px', color: '#555' }}>{message}</p>
        <div style={{ display: 'flex', justifyContent: 'space-around' }}>
          <button
            onClick={onConfirm}
            style={{
              padding: '10px 20px',
              borderRadius: '5px',
              border: 'none',
              backgroundColor: '#dc3545',
              color: 'white',
              cursor: 'pointer',
              fontSize: '1em'
            }}
          >
            확인
          </button>
          <button
            onClick={onClose}
            style={{
              padding: '10px 20px',
              borderRadius: '5px',
              border: '1px solid #ccc',
              backgroundColor: '#f8f9fa',
              color: '#333',
              cursor: 'pointer',
              fontSize: '1em'
            }}
          >
            취소
          </button>
        </div>
      </div>
    </div>
  );
};

export default ConfirmationDialog;
