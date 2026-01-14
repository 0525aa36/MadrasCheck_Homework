import React, { useState } from 'react';
import { fileApi } from '../services/api';
import { useNotification } from '../contexts/NotificationContext';
import './FileExtensionChecker.css';

const FileExtensionChecker = () => {
  const [selectedFile, setSelectedFile] = useState(null);
  const [isBlocked, setIsBlocked] = useState(null);
  const [loading, setLoading] = useState(false);
  const [isDragging, setIsDragging] = useState(false);
  const { showNotification } = useNotification();

  const handleFileChange = (event) => {
    const file = event.target.files[0];
    if (file) {
      setSelectedFile(file);
      setIsBlocked(null);
      event.target.value = null;
    }
  };

  const handleDragOver = (event) => {
    event.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = () => {
    setIsDragging(false);
  };

  const handleDrop = (event) => {
    event.preventDefault();
    setIsDragging(false);
    const file = event.dataTransfer.files[0];
    if (file) {
      setSelectedFile(file);
      setIsBlocked(null);
    }
  };

  const handleCheckExtension = async () => {
    if (!selectedFile) {
      showNotification('파일을 선택하거나 드래그 앤 드롭해주세요.', 'error');
      return;
    }

    setLoading(true);
    const formData = new FormData();
    formData.append('file', selectedFile);

    try {
      const response = await fileApi.checkFileExtension(formData);
      setIsBlocked(response.data.data);
      const extension = getFileExtension(selectedFile.name);
      if (response.data.data) {
        showNotification(`확장자 '.${extension}'는 차단되었습니다.`, 'error');
      } else {
        showNotification(`확장자 '.${extension}'는 차단되지 않았습니다.`, 'success');
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
    const lastDotIndex = filename.lastIndexOf('.');
    if (lastDotIndex > 0 && lastDotIndex < filename.length - 1) {
      return filename.substring(lastDotIndex + 1);
    }
    return '';
  };

  const formatFileSize = (bytes) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
  };

  return (
    <div className="file-checker">
      <div className="file-checker-header">
        <h2>파일 확장자 차단 여부 확인</h2>
        <p className="file-checker-description">
          파일을 업로드하여 확장자가 차단되었는지 확인하세요
        </p>
      </div>

      <input
        id="fileInput"
        type="file"
        onChange={handleFileChange}
        style={{ display: 'none' }}
      />

      {!selectedFile ? (
        <div 
          className={`file-drop-zone ${isDragging ? 'dragging' : ''}`}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
          onClick={() => document.getElementById('fileInput').click()}
        >
          <div className="file-drop-icon">📁</div>
          <p className="file-drop-text">파일을 드래그 앤 드롭하거나 클릭하여 선택하세요</p>
          <p className="file-drop-subtext">모든 파일 형식 지원</p>
        </div>
      ) : (
        <div className="file-selected">
          <div className="file-icon">📄</div>
          <div className="file-info">
            <p className="file-name">{selectedFile.name}</p>
            <p className="file-size">{formatFileSize(selectedFile.size)}</p>
          </div>
          <button 
            onClick={(e) => {
              e.stopPropagation();
              setSelectedFile(null);
              setIsBlocked(null);
            }}
            style={{
              background: 'none',
              border: 'none',
              fontSize: '24px',
              cursor: 'pointer',
              color: '#868e96'
            }}
          >
            ×
          </button>
        </div>
      )}

      <button 
        onClick={handleCheckExtension}
        disabled={!selectedFile || loading}
        className="file-checker-button"
      >
        {loading ? '확인 중...' : '확장자 확인'}
      </button>

      {isBlocked !== null && (
        <div className={`file-checker-result ${isBlocked ? 'blocked' : 'allowed'}`}>
          {isBlocked ? (
            <>
              🚫 이 파일의 확장자는 <strong>차단</strong>되었습니다
            </>
          ) : (
            <>
              ✅ 이 파일의 확장자는 <strong>허용</strong>됩니다
            </>
          )}
        </div>
      )}
    </div>
  );
};

export default FileExtensionChecker;
