import React, { useState } from 'react';
import { fileApi } from '../services/api';
import { useNotification } from '../contexts/NotificationContext';

const FileExtensionChecker = () => {
  const [selectedFile, setSelectedFile] = useState(null);
  const [isBlocked, setIsBlocked] = useState(null); // null: initial, true: blocked, false: not blocked
  const [loading, setLoading] = useState(false);
  const { showNotification } = useNotification();

  const handleFileChange = (event) => {
    setSelectedFile(event.target.files[0]);
    setIsBlocked(null); // Reset status when a new file is selected
  };

  const handleCheckExtension = async () => {
    if (!selectedFile) {
      showNotification('파일을 선택해주세요.', 'error');
      return;
    }

    setLoading(true);
    const formData = new FormData();
    formData.append('file', selectedFile);

    try {
      const response = await fileApi.checkFileExtension(formData);
      setIsBlocked(response.data.data);
      if (response.data.data) {
        showNotification(`확장자 '.${getFileExtension(selectedFile.name)}'는 차단되었습니다.`, 'error');
      } else {
        showNotification(`확장자 '.${getFileExtension(selectedFile.name)}'는 차단되지 않았습니다.`, 'success');
      }
    } catch (error) {
      console.error('파일 확장자 확인 실패:', error);
      showNotification('파일 확장자 확인 중 오류가 발생했습니다.', 'error');
      setIsBlocked(null);
    } finally {
      setLoading(false);
    }
  };

  const getFileExtension = (filename) => {
    return filename.slice((filename.lastIndexOf('.') - 1 >>> 0) + 2);
  };

  return (
    <div style={{ border: '1px solid #ccc', padding: '15px', borderRadius: '8px', marginBottom: '20px' }}>
      <h2 style={{ fontSize: '1.2em', marginBottom: '10px' }}>파일 확장자 차단 여부 확인</h2>
      <input type="file" onChange={handleFileChange} style={{ marginBottom: '10px' }} />
      <button 
        onClick={handleCheckExtension} 
        disabled={!selectedFile || loading}
        style={{ padding: '8px 15px', borderRadius: '4px', border: 'none', backgroundColor: '#28a745', color: 'white', cursor: 'pointer' }}
      >
        {loading ? '확인 중...' : '확장자 확인'}
      </button>

      {isBlocked !== null && (
        <p style={{ marginTop: '15px', fontWeight: 'bold', color: isBlocked ? 'red' : 'green' }}>
          {isBlocked ? `'.${getFileExtension(selectedFile.name)}' 확장자는 차단되었습니다.` : `'.${getFileExtension(selectedFile.name)}' 확장자는 차단되지 않았습니다.`}
        </p>
      )}
    </div>
  );
};

export default FileExtensionChecker;
