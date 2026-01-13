import React, { useState } from 'react';
import { fileApi } from '../services/api';
import { useNotification } from '../contexts/NotificationContext';

const FileExtensionChecker = () => {
  const [selectedFile, setSelectedFile] = useState(null);
  const [isBlocked, setIsBlocked] = useState(null); // null: initial, true: blocked, false: not blocked
  const [loading, setLoading] = useState(false);
  const [isDragging, setIsDragging] = useState(false); // State for drag-and-drop visual feedback
  const { showNotification } = useNotification();

  const handleFileChange = (event) => {
    const file = event.target.files[0];
    if (file) {
      setSelectedFile(file);
      setIsBlocked(null); // Reset status when a new file is selected
      event.target.value = null; // Clear the input value to allow selecting the same file again if needed
    }
  };

  const handleDragOver = (event) => {
    event.preventDefault(); // Prevent default to allow drop
    setIsDragging(true);
  };

  const handleDragLeave = () => {
    setIsDragging(false);
  };

  const handleDrop = (event) => {
    event.preventDefault(); // Prevent default file opening
    setIsDragging(false);
    const file = event.dataTransfer.files[0];
    if (file) {
      setSelectedFile(file);
      setIsBlocked(null); // Reset status when a new file is dropped
      console.log("Dropped file:", file); // Add this for debugging
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
    return ''; // No extension or invalid
  };

  return (
    <div style={{ border: '1px solid #ccc', padding: '15px', borderRadius: '8px', marginBottom: '20px' }}>
      <h2 style={{ fontSize: '1.2em', marginBottom: '10px' }}>파일 확장자 차단 여부 확인</h2>
      
      <div 
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        onClick={() => document.getElementById('fileInput').click()} // Trigger hidden input on click
        style={{
          border: `2px dashed ${isDragging ? '#007bff' : '#ccc'}`,
          borderRadius: '8px',
          padding: '20px',
          textAlign: 'center',
          cursor: 'pointer',
          marginBottom: '10px',
          backgroundColor: isDragging ? '#e6f7ff' : '#f8f8f8',
          position: 'relative', // Needed for absolute positioning of input
        }}
      >
        {selectedFile ? (
          <p>선택된 파일: <strong>{selectedFile.name}</strong></p>
        ) : (
          <p>파일을 여기에 드래그 앤 드롭하거나 클릭하여 선택하세요.</p>
        )}
        <input 
          type="file" 
          id="fileInput" // Add an ID
          onChange={handleFileChange} 
          style={{ 
            opacity: 0, 
            position: 'absolute', 
            width: '100%', 
            height: '100%', 
            top: 0, 
            left: 0, 
            cursor: 'pointer' 
          }} 
        />
      </div>

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
