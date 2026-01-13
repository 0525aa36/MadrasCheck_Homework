import React, { useState } from 'react';
import FixedExtensions from './components/FixedExtensions';
import ExtensionInput from './components/ExtensionInput';
import CustomExtensions from './components/CustomExtensions';
import './App.css'; // Keep App.css for basic app styling if needed, or remove if all styling is in index.css

function App() {
  const [customExtensionsRefreshTrigger, setCustomExtensionsRefreshTrigger] = useState(0);

  const handleCustomExtensionAdded = () => {
    setCustomExtensionsRefreshTrigger(prev => prev + 1);
  };

  return (
    <div className="App" style={{ maxWidth: '800px', margin: '40px auto', padding: '20px', border: '1px solid #eee', borderRadius: '10px', boxShadow: '0 4px 8px rgba(0,0,0,0.05)', fontFamily: 'Arial, sans-serif' }}>
      <h1 style={{ textAlign: 'center', color: '#333', marginBottom: '30px' }}>파일 확장자 차단 시스템</h1>
      
      <FixedExtensions />
      
      <div style={{ marginBottom: '20px' }}>
        <h2 style={{ fontSize: '1.2em', marginBottom: '10px' }}>커스텀 확장자 추가</h2>
        <ExtensionInput onAdd={handleCustomExtensionAdded} />
      </div>

      <CustomExtensions refreshTrigger={customExtensionsRefreshTrigger} />
    </div>
  );
}

export default App;
