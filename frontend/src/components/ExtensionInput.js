import React, { useState } from 'react';
import { extensionApi } from '../services/api';
import { useNotification } from '../contexts/NotificationContext';
import './ExtensionInput.css';

const ExtensionInput = ({ onAdd }) => {
  const [extension, setExtension] = useState('');
  const [error, setError] = useState('');
  const { showNotification } = useNotification();

  const validateExtension = (value) => {
    if (!value.trim()) return '확장자를 입력해주세요';
    if (value.length > 20) return '최대 20자까지 입력 가능합니다';
    if (!/^[a-zA-Z0-9]+$/.test(value.replace('.', ''))) {
      return '영문과 숫자만 가능합니다';
    }
    return '';
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    const validationError = validateExtension(extension);
    if (validationError) {
      setError(validationError);
      return;
    }

    try {
      const response = await extensionApi.addCustomExtension(extension);
      setExtension('');
      setError('');
      onAdd(response.data.data);
      showNotification('확장자가 추가되었습니다.', 'success');
    } catch (err) {
      const errorMessage = err.response?.data?.message || '확장자 추가에 실패했습니다.';
      showNotification(errorMessage, 'error');
      console.error('확장자 추가 실패:', err);
    }
  };

  return (
    <div className="extension-input-container">
      <div className="extension-input-header">
        <h2>커스텀 확장자 추가</h2>
        <p className="extension-input-description">
          영문과 숫자로만 구성된 확장자를 추가할 수 있습니다 (최대 20자)
        </p>
      </div>

      <form onSubmit={handleSubmit} className="extension-input-form">
        <div className="extension-input-wrapper">
          <input
            type="text"
            value={extension}
            onChange={(e) => {
              setExtension(e.target.value);
              setError('');
            }}
            placeholder="예: pdf, jpg, zip"
            maxLength={20}
            className="extension-input-field"
          />
          {error && <div style={{ color: '#fa5252', fontSize: '13px', marginTop: '8px' }}>{error}</div>}
        </div>
        <button type="submit" className="extension-input-button">
          추가
        </button>
      </form>
    </div>
  );
};

export default ExtensionInput;
