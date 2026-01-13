import React, { useState, useEffect } from 'react';
import { extensionApi } from '../services/api';
import { useNotification } from '../contexts/NotificationContext';
import ConfirmationDialog from './ConfirmationDialog'; // Import ConfirmationDialog

const CustomExtensions = ({ refreshTrigger }) => {
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
    setIsConfirmDialogOpen(false); // Close dialog first
    if (extensionToDeleteId === null) return;

    try {
      await extensionApi.deleteCustomExtension(extensionToDeleteId);
      setExtensions(extensions.filter(ext => ext.id !== extensionToDeleteId));
      showNotification('확장자가 삭제되었습니다.', 'success');
    } catch (error) {
      console.error('커스텀 확장자 삭제 실패:', error);
      showNotification('확장자 삭제에 실패했습니다: ' + (error.response?.data?.message || error.message), 'error');
    } finally {
      setExtensionToDeleteId(null); // Reset
    }
  };

  const cancelDelete = () => {
    setIsConfirmDialogOpen(false);
    setExtensionToDeleteId(null);
  };

  useEffect(() => {
    fetchExtensions();
  }, [refreshTrigger]);

  if (loading) return <div>로딩중...</div>;

  return (
    <div style={{ border: '1px solid #ccc', padding: '15px', borderRadius: '8px' }}>
      <h2 style={{ fontSize: '1.2em', marginBottom: '10px' }}>커스텀 확장자 ({extensions.length} / 200)</h2>
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '10px' }}>
        {extensions.map(ext => (
          <span key={ext.id} style={{ display: 'flex', alignItems: 'center', backgroundColor: '#f0f0f0', padding: '5px 10px', borderRadius: '15px', border: '1px solid #ddd' }}>
            {ext.extension}
            <button 
              onClick={() => handleDeleteClick(ext.id)} // Use handleDeleteClick
              style={{ marginLeft: '8px', background: 'none', border: 'none', color: '#dc3545', cursor: 'pointer', fontSize: '1em', padding: '0', lineHeight: '1' }}
            >
              &times;
            </button>
          </span>
        ))}
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
