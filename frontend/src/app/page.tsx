"use client";

import React, { useState } from 'react';

const API_BASE_URL = 'http://localhost:8080/compiler';

type TabType = 'lexer' | 'parser' | 'semantic' | 'tac' | 'quadruple' | 'target' | 'full';

interface Token {
  type: string;
  value: string;
  line: number;
}

export default function Home() {
  const [code, setCode] = useState('ranger MyProgram {\n    int a = 10;\n    int b = 20;\n    int c = a + b;\n    print(c);\n}\n');
  const [activeTab, setActiveTab] = useState<TabType>('lexer');
  const [isLoading, setIsLoading] = useState(false);
  const [results, setResults] = useState<Record<string, any>>({});
  const [error, setError] = useState<string | null>(null);

  const runCompiler = async () => {
    setIsLoading(true);
    setError(null);
    const newResults: Record<string, any> = {};

    try {
      // 1. Lexer
      const lexerRes = await fetch(`${API_BASE_URL}/phase1/lexer`, {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain' },
        body: code,
      });
      if (lexerRes.ok) {
        newResults.lexer = await lexerRes.json();
      } else {
        throw new Error(await lexerRes.text());
      }

      // 2. Parser
      const parserRes = await fetch(`${API_BASE_URL}/phase2/parser`, {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain' },
        body: code,
      });
      newResults.parser = await parserRes.text();

      // 3. Semantic
      const semanticRes = await fetch(`${API_BASE_URL}/phase3/semantic`, {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain' },
        body: code,
      });
      newResults.semantic = await semanticRes.text();

      // 4. TAC
      const tacRes = await fetch(`${API_BASE_URL}/phase4/tac`, {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain' },
        body: code,
      });
      newResults.tac = await tacRes.text();

      // 5. Quadruple
      const quadRes = await fetch(`${API_BASE_URL}/phase4/quadruple`, {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain' },
        body: code,
      });
      newResults.quadruple = await quadRes.text();

      // 6. Target Code
      const targetRes = await fetch(`${API_BASE_URL}/phase6/target`, {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain' },
        body: code,
      });
      newResults.target = await targetRes.text();

      // Full Compile
      const fullRes = await fetch(`${API_BASE_URL}/compile/full`, {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain' },
        body: code,
      });
      if (fullRes.ok) {
        newResults.full = await fullRes.json();
      } else {
        newResults.full = await fullRes.text();
      }

    } catch (err: any) {
      setError(err.message || "An error occurred during compilation.");
    } finally {
      setResults(newResults);
      setIsLoading(false);
    }
  };

  const renderOutput = () => {
    if (error) {
      return <div className="output-content" style={{ color: 'var(--error)' }}>{error}</div>;
    }

    const data = results[activeTab];

    if (!data && !isLoading) {
      return (
        <div className="empty-state">
          <svg xmlns="http://www.w3.org/-2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" d="M14.25 9.75L16.5 12l-2.25 2.25m-4.5 0L7.5 12l2.25-2.25M6 20.25h12A2.25 2.25 0 0020.25 18V6A2.25 2.25 0 0018 3.75H6A2.25 2.25 0 003.75 6v12A2.25 2.25 0 006 20.25z" />
          </svg>
          <p>Run the compiler to see results</p>
        </div>
      );
    }

    if (isLoading) {
      return (
        <div className="empty-state">
          <div className="loader"></div>
          <p style={{ marginTop: '1rem' }}>Compiling...</p>
        </div>
      );
    }

    if (activeTab === 'lexer' && Array.isArray(data)) {
      return (
        <div className="output-content" style={{ padding: 0 }}>
          <table className="token-table">
            <thead>
              <tr>
                <th>Type</th>
                <th>Value</th>
                <th>Line</th>
              </tr>
            </thead>
            <tbody>
              {data.map((token: Token, idx: number) => (
                <tr key={idx} className="animated" style={{ animationDelay: `${idx * 0.02}s` }}>
                  <td className="token-type">{token.type}</td>
                  <td className="token-value">{token.value}</td>
                  <td>{token.line}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      );
    }

    if (activeTab === 'full' && typeof data === 'object') {
      return (
        <div className="output-content">
          {JSON.stringify(data, null, 2)}
        </div>
      );
    }

    return (
      <div className="output-content animated">
        {typeof data === 'string' ? data : JSON.stringify(data, null, 2)}
      </div>
    );
  };

  const tabs: { id: TabType, label: string }[] = [
    { id: 'lexer', label: 'Tokens' },
    { id: 'parser', label: 'AST' },
    { id: 'semantic', label: 'Semantic' },
    { id: 'tac', label: 'TAC' },
    { id: 'quadruple', label: 'Quadruple' },
    { id: 'target', label: 'Target Code' },
    { id: 'full', label: 'Full Report' },
  ];

  return (
    <div className="app-container">
      <header className="header">
        <div className="logo">
          <div className="logo-icon">MC</div>
          <div className="logo-text">Mini-Compiler Tool Kit</div>
        </div>
        <button className="btn btn-primary" onClick={runCompiler} disabled={isLoading}>
          {isLoading ? <><div className="loader"></div> Compiling...</> : (
            <>
              <svg xmlns="http://www.w3.org/-2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>
              Run Compiler
            </>
          )}
        </button>
      </header>

      <main className="main-content">
        <section className="editor-section animated" style={{ animationDelay: '0.1s' }}>
          <div className="section-header">
            <div className="section-title">Source Code</div>
          </div>
          <textarea 
            className="code-editor" 
            value={code}
            onChange={(e) => setCode(e.target.value)}
            spellCheck="false"
          />
        </section>

        <section className="output-section animated" style={{ animationDelay: '0.2s' }}>
          <div className="tabs">
            {tabs.map(tab => (
              <button 
                key={tab.id}
                className={`tab ${activeTab === tab.id ? 'active' : ''}`}
                onClick={() => setActiveTab(tab.id)}
              >
                {tab.label}
              </button>
            ))}
          </div>
          {renderOutput()}
        </section>
      </main>
    </div>
  );
}
