import React, { useState, useEffect } from 'react';
import { extensionApi } from '../services/api';
import { useNotification } from '../contexts/NotificationContext'; // Import useNotification

const FixedExtensions = () => {
  const [extensions, setExtensions] = useState([]);
  const [loading, setLoading] = useState(true);
  const { showNotification } = useNotification(); // Use the notification hook

  // 단일 책임: 데이터 가져오기
  const fetchExtensions = async () => {
    try {
      setLoading(true);
      const response = await extensionApi.getFixedExtensions();
      setExtensions(response.data.data);
    } catch (error) {
      console.error('고정 확장자 조회 실패:', error);
      showNotification('고정 확장자 조회에 실패했습니다.', 'error'); // Use showNotification
    } finally {
      setLoading(false);
    }
  };

  // 단일 책임: 상태 토글
  const handleToggle = async (id, currentStatus) => {
    try {
      await extensionApi.updateFixedExtension(id, !currentStatus);
      setExtensions(extensions.map(ext => 
        ext.id === id ? { ...ext, blocked: !currentStatus } : ext
      ));
    } catch (error) {
      console.error('고정 확장자 업데이트 실패:', error);
      showNotification('고정 확장자 업데이트에 실패했습니다: ' + (error.response?.data?.message || error.message), 'error'); // Use showNotification
    }
  };

  useEffect(() => {
    fetchExtensions();
  }, []);

  if (loading) return <div>로딩중...</div>;

  return (
    <div style={{ border: '1px solid #ccc', padding: '15px', borderRadius: '8px', marginBottom: '20px' }}>
      <h2 style={{ fontSize: '1.2em', marginBottom: '10px' }}>고정 확장자</h2>
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '10px' }}>
        {extensions.map(ext => (
          <label key={ext.id} style={{ display: 'flex', alignItems: 'center', cursor: 'pointer', padding: '5px 8px', border: '1px solid #eee', borderRadius: '4px', backgroundColor: ext.blocked ? '#ffe0e0' : '#e0ffe0' }}>
            <input
              type="checkbox"
              checked={ext.blocked}
              onChange={() => handleToggle(ext.id, ext.blocked)}
              style={{ marginRight: '5px' }}
            />
            {ext.extension}
          </label>
        ))}
      </div>
    </div>
  );
};

export default FixedExtensions;
