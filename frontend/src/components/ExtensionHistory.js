import React, { useState, useEffect } from 'react';
import { extensionApi } from '../services/api';
import './ExtensionHistory.css';

const ExtensionHistory = () => {
  const [extensions, setExtensions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all'); // all, fixed, custom, blocked

  const fetchExtensions = async () => {
    try {
      setLoading(true);
      const [fixedRes, customRes] = await Promise.all([
        extensionApi.getFixedExtensions(),
        extensionApi.getCustomExtensions()
      ]);
      
      const allExtensions = [
        ...fixedRes.data.data,
        ...customRes.data.data
      ].sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt));
      
      setExtensions(allExtensions);
    } catch (error) {
      console.error('확장자 히스토리 조회 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchExtensions();
    // 자동 새로고침 제거 - 사용자 액션에만 반응
  }, []);

  const filteredExtensions = extensions.filter(ext => {
    if (filter === 'all') return true;
    if (filter === 'fixed') return ext.fixed;
    if (filter === 'custom') return !ext.fixed;
    if (filter === 'blocked') return ext.blocked;
    return true;
  });

  const getActionText = (ext) => {
    if (ext.fixed) {
      return ext.blocked ? '차단됨' : '차단 해제됨';
    }
    return '추가됨';
  };

  const getActionColor = (ext) => {
    if (ext.blocked) return '#ff6b6b';
    if (ext.fixed && !ext.blocked) return '#51cf66';
    return '#339af0';
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return '방금 전';
    if (diffMins < 60) return `${diffMins}분 전`;
    if (diffHours < 24) return `${diffHours}시간 전`;
    if (diffDays < 7) return `${diffDays}일 전`;
    
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  if (loading) {
    return <div className="history-loading">로딩중...</div>;
  }

  return (
    <div className="extension-history">
      <div className="history-header">
        <h2>확장자 관리 기록</h2>
        <div className="history-filters">
          <button 
            className={filter === 'all' ? 'active' : ''} 
            onClick={() => setFilter('all')}
          >
            전체 ({extensions.length})
          </button>
          <button 
            className={filter === 'blocked' ? 'active' : ''} 
            onClick={() => setFilter('blocked')}
          >
            차단됨 ({extensions.filter(e => e.blocked).length})
          </button>
          <button 
            className={filter === 'fixed' ? 'active' : ''} 
            onClick={() => setFilter('fixed')}
          >
            고정 ({extensions.filter(e => e.fixed).length})
          </button>
          <button 
            className={filter === 'custom' ? 'active' : ''} 
            onClick={() => setFilter('custom')}
          >
            커스텀 ({extensions.filter(e => !e.fixed).length})
          </button>
        </div>
      </div>

      <div className="history-list">
        {filteredExtensions.map(ext => (
          <div key={ext.id} className="history-item">
            <div className="history-item-icon">
              <span 
                className="extension-badge" 
                style={{ backgroundColor: getActionColor(ext) }}
              >
                .{ext.extension}
              </span>
            </div>
            
            <div className="history-item-content">
              <div className="history-item-main">
                <span className="history-extension-name">.{ext.extension}</span>
                <span 
                  className="history-action" 
                  style={{ color: getActionColor(ext) }}
                >
                  {getActionText(ext)}
                </span>
                {ext.fixed && (
                  <span className="history-badge-fixed">고정</span>
                )}
              </div>
              
              <div className="history-item-meta">
                {ext.updatedByName && (
                  <span className="history-user">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                    </svg>
                    {ext.updatedByName}
                  </span>
                )}
                <span className="history-time">{formatDate(ext.updatedAt)}</span>
              </div>
            </div>
          </div>
        ))}

        {filteredExtensions.length === 0 && (
          <div className="history-empty">
            <p>기록이 없습니다</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default ExtensionHistory;
