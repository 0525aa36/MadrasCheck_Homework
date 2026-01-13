import React, { useState } from 'react';
import { extensionApi } from '../services/api';
import { useNotification } from '../contexts/NotificationContext'; // Import useNotification

const ExtensionInput = ({ onAdd }) => {
  const [extension, setExtension] = useState('');
  const [error, setError] = useState('');
  const { showNotification } = useNotification(); // Use the notification hook

  // 단일 책임: 입력 검증
  const validateExtension = (value) => {
    if (!value.trim()) return '확장자를 입력해주세요';
    if (value.length > 20) return '최대 20자까지 입력 가능합니다';
    if (!/^[a-zA-Z0-9]+$/.test(value.replace('.', ''))) {
      return '영문과 숫자만 가능합니다';
    }
    return '';
  };

  // 단일 책임: 제출 처리
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
      onAdd(response.data.data); // 부모 컴포넌트에 추가된 확장자 전달
      showNotification('확장자가 추가되었습니다.', 'success'); // Use showNotification
    } catch (err) {
      const errorMessage = err.response?.data?.message || '확장자 추가에 실패했습니다.';
      showNotification(errorMessage, 'error'); // Use showNotification for API errors
      console.error('확장자 추가 실패:', err);
    }
  };

  return (
    <form onSubmit={handleSubmit} style={{ marginBottom: '20px', display: 'flex', flexDirection: 'column', gap: '10px' }}>
      <div style={{ display: 'flex', gap: '10px' }}>
        <input
          type="text"
          value={extension}
          onChange={(e) => {
            setExtension(e.target.value);
            setError(''); // 입력 시 에러 메시지 초기화
          }}
          placeholder="확장자 입력 (예: pdf)"
          maxLength={20}
          style={{ flexGrow: 1, padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
        />
        <button type="submit" style={{ padding: '8px 15px', borderRadius: '4px', border: 'none', backgroundColor: '#007bff', color: 'white', cursor: 'pointer' }}>추가</button>
      </div>
      {error && <div style={{ color: 'red', fontSize: '0.9em' }}>{error}</div>}
    </form>
  );
};

export default ExtensionInput;
