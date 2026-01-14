import React, { useState, useEffect } from 'react';
import { extensionApi } from '../services/api';
import { useNotification } from '../contexts/NotificationContext';
import ConfirmationDialog from './ConfirmationDialog';
import './CustomExtensions.css';

const CustomExtensions = ({ refreshTrigger, onUpdate }) => {
  const [extensions, setExtensions] = useState([]);
  const [loading, setLoading] = useState(true);
  const { showNotification } = useNotification();

  const [isConfirmDialogOpen, setIsConfirmDialogOpen] = useState(false);
  const [extensionToDeleteId, setExtensionToDeleteId] = useState(null);

  const fetchExtensions = async () => {
    try {
      setLoading(true);
      const response = await extensionApi.getCustomExtensions();
      setExtensions(response.data.data);
    } catch (error) {
      console.error('커스텀 확장자 조회 실패:', error);
      showNotification('커스텀 확장자 조회에 실패했습니다.', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteClick = (id) => {
    setExtensionToDeleteId(id);
    setIsConfirmDialogOpen(true);
  };

  const confirmDelete = async () => {
    setIsConfirmDialogOpen(false);
    if (extensionToDeleteId === null) return;

    try {
      await extensionApi.deleteCustomExtension(extensionToDeleteId);
      setExtensions(extensions.filter(ext => ext.id !== extensionToDeleteId));
      showNotification('확장자가 삭제되었습니다.', 'success');
      if (onUpdate) onUpdate();
    } catch (error) {
      console.error('커스텀 확장자 삭제 실패:', error);
      showNotification('확장자 삭제에 실패했습니다: ' + (error.response?.data?.message || error.message), 'error');
    } finally {
      setExtensionToDeleteId(null);
    }
  };

  const cancelDelete = () => {
    setIsConfirmDialogOpen(false);
    setExtensionToDeleteId(null);
  };

  useEffect(() => {
    fetchExtensions();
  }, [refreshTrigger]);

  if (loading) {
    return (
      <div className="custom-extensions">
        <div className="custom-extensions-loading">로딩중...</div>
      </div>
    );
  }

  return (
    <div className="custom-extensions">
      <div className="custom-extensions-header">
        <h2>커스텀 확장자</h2>
        <span className="custom-extensions-count">
          {extensions.length} / 200
        </span>
      </div>

      <div className="custom-extensions-list">
        {extensions.length === 0 ? (
          <div className="custom-extensions-empty">
            추가된 커스텀 확장자가 없습니다
          </div>
        ) : (
          extensions.map(ext => (
            <div key={ext.id} className="custom-extension-tag">
              <span className="custom-extension-name">.{ext.extension}</span>
              <button 
                onClick={() => handleDeleteClick(ext.id)}
                className="custom-extension-delete"
                title="삭제"
              >
                ×
              </button>
            </div>
          ))
        )}
      </div>

      <ConfirmationDialog
        isOpen={isConfirmDialogOpen}
        onClose={cancelDelete}
        onConfirm={confirmDelete}
        title="확장자 삭제 확인"
        message="정말로 이 확장자를 삭제하시겠습니까?"
      />
    </div>
  );
};

export default CustomExtensions;
