import React, { useState, useEffect } from 'react';
import { extensionApi } from '../services/api';
import { useNotification } from '../contexts/NotificationContext';
import './FixedExtensions.css';

const FixedExtensions = ({ onUpdate, isAuthenticated, onLoginRequired }) => {
  const [extensions, setExtensions] = useState([]);
  const [loading, setLoading] = useState(true);
  const { showNotification } = useNotification();

  const fetchExtensions = async () => {
    try {
      setLoading(true);
      const response = await extensionApi.getFixedExtensions();
      setExtensions(response.data.data);
    } catch (error) {
      console.error('고정 확장자 조회 실패:', error);
      showNotification('고정 확장자 조회에 실패했습니다.', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleToggle = async (id, currentStatus) => {
    if (!isAuthenticated) {
      showNotification('확장자를 수정하려면 로그인이 필요합니다.', 'warning');
      if (onLoginRequired) onLoginRequired();
      return;
    }

    try {
      await extensionApi.updateFixedExtension(id, !currentStatus);
      setExtensions(extensions.map(ext =>
        ext.id === id ? { ...ext, blocked: !currentStatus } : ext
      ));
      if (onUpdate) onUpdate();
      showNotification('고정 확장자가 수정되었습니다.', 'success');
    } catch (error) {
      console.error('고정 확장자 업데이트 실패:', error);
      if (error.response?.status === 401 || error.response?.status === 403) {
        showNotification('로그인이 필요합니다.', 'error');
        if (onLoginRequired) onLoginRequired();
      } else {
        showNotification('고정 확장자 업데이트에 실패했습니다: ' + (error.response?.data?.message || error.message), 'error');
      }
    }
  };

  useEffect(() => {
    fetchExtensions();
  }, []);

  if (loading) {
    return (
      <div className="fixed-extensions">
        <div className="fixed-extensions-loading">로딩중...</div>
      </div>
    );
  }

  return (
    <div className="fixed-extensions">
      <div className="fixed-extensions-header">
        <h2>고정 확장자</h2>
        <p className="fixed-extensions-description">
          자주 차단하는 확장자입니다. 체크하면 해당 확장자가 차단됩니다.
        </p>
      </div>
      
      <div className="fixed-extensions-grid">
        {extensions.map(ext => (
          <div 
            key={ext.id} 
            className={`fixed-extension-item ${ext.blocked ? 'blocked' : ''}`}
            onClick={() => handleToggle(ext.id, ext.blocked)}
          >
            <label className="fixed-extension-label">
              <input
                type="checkbox"
                checked={ext.blocked}
                onChange={() => {}}
                className="fixed-extension-checkbox"
              />
              <span className="fixed-extension-name">.{ext.extension}</span>
            </label>
          </div>
        ))}
      </div>
    </div>
  );
};

export default FixedExtensions;
