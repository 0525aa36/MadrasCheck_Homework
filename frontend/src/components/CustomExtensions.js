import React, { useState, useEffect } from 'react';
import { extensionApi } from '../services/api';

const CustomExtensions = ({ refreshTrigger }) => {
  const [extensions, setExtensions] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchExtensions = async () => {
    try {
      setLoading(true);
      const response = await extensionApi.getCustomExtensions();
      setExtensions(response.data.data);
    } catch (error) {
      console.error('커스텀 확장자 조회 실패:', error);
      alert('커스텀 확장자 조회에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('정말로 삭제하시겠습니까?')) return;
    
    try {
      await extensionApi.deleteCustomExtension(id);
      setExtensions(extensions.filter(ext => ext.id !== id));
      alert('확장자가 삭제되었습니다.');
    } catch (error) {
      console.error('커스텀 확장자 삭제 실패:', error);
      alert('확장자 삭제에 실패했습니다: ' + (error.response?.data?.message || error.message));
    }
  };

  useEffect(() => {
    fetchExtensions();
  }, [refreshTrigger]); // refreshTrigger가 변경될 때마다 목록을 새로고침

  if (loading) return <div>로딩중...</div>;

  return (
    <div style={{ border: '1px solid #ccc', padding: '15px', borderRadius: '8px' }}>
      <h2 style={{ fontSize: '1.2em', marginBottom: '10px' }}>커스텀 확장자 ({extensions.length} / 200)</h2>
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '10px' }}>
        {extensions.map(ext => (
          <span key={ext.id} style={{ display: 'flex', alignItems: 'center', backgroundColor: '#f0f0f0', padding: '5px 10px', borderRadius: '15px', border: '1px solid #ddd' }}>
            {ext.extension}
            <button 
              onClick={() => handleDelete(ext.id)} 
              style={{ marginLeft: '8px', background: 'none', border: 'none', color: '#dc3545', cursor: 'pointer', fontSize: '1em', padding: '0', lineHeight: '1' }}
            >
              &times;
            </button>
          </span>
        ))}
      </div>
    </div>
  );
};

export default CustomExtensions;
