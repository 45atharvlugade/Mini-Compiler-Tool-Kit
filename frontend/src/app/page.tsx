"use client";

import React, { useState } from 'react';

const API_BASE_URL = 'http://localhost:4041/compiler';

type TabType = 'lexer' | 'parser' | 'semantic' | 'tac' | 'quadruple' | 'triples' | 'indirectTriples' | 'target' | 'full' | 'execution';

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

    const fetchPhase = async (path: string, key: string, isJson: boolean = false) => {
      try {
        const res = await fetch(`${API_BASE_URL}${path}`, {
          method: 'POST',
          headers: { 'Content-Type': 'text/plain' },
          body: code,
        });
        
        if (isJson) {
          if (res.ok) newResults[key] = await res.json();
          else newResults[key] = await res.text(); // Capture error text
        } else {
          newResults[key] = await res.text();
        }
      } catch (e: any) {
        newResults[key] = e.message || "Network Error";
      }
    };

    await Promise.all([
      fetchPhase('/phase1/lexer', 'lexer', true),
      fetchPhase('/phase2/parser/text', 'parser', false),
      fetchPhase('/phase3/semantic', 'semantic', false),
      fetchPhase('/phase4/ir-tables', 'ir', true),
      fetchPhase('/phase6/target', 'target', false),
      fetchPhase('/all', 'full', true),
      fetchPhase('/compile', 'execution', false)
    ]);

    if (newResults.ir && !newResults.ir.error && typeof newResults.ir === 'object') {
      newResults.tac = newResults.ir.tac;
      newResults.quadruple = newResults.ir.quadruples;
      newResults.triples = newResults.ir.triples;
      newResults.indirectTriples = newResults.ir.indirectTriples;
    } else {
      newResults.tac = newResults.ir;
      newResults.quadruple = newResults.ir;
      newResults.triples = newResults.ir;
      newResults.indirectTriples = newResults.ir;
    }

    setResults(newResults);
    setIsLoading(false);
  };

  const isErrorText = (text: any) => {
    if (typeof text !== 'string') return false;
    return text.includes('"status":500') || text.includes('Exception') || text.includes('Error:');
  };

  const renderError = (data: string) => {
    try {
      const parsed = JSON.parse(data);
      return (
        <div className="error-state">
          <strong>{parsed.error || 'Error'} ({parsed.status})</strong>
          <p style={{ marginTop: '0.5rem' }}>{parsed.message}</p>
          {parsed.trace && (
            <details style={{ marginTop: '1rem', cursor: 'pointer' }}>
              <summary>View Stack Trace</summary>
              <pre style={{ marginTop: '0.5rem', fontSize: '0.8rem', opacity: 0.8 }}>{parsed.trace}</pre>
            </details>
          )}
        </div>
      );
    } catch {
      return <div className="error-state">{data}</div>;
    }
  };

  // Structured Formatters
  const renderSemantic = (data: string) => {
    let errors: string[] = [];
    let symbolTable: string[] = [];
    let typeTable: string[] = [];
    let expressions: string[] = [];
    let constants: string[] = [];
    let strings: string[] = [];
    let temps: string[] = [];

    let currentSection = '';

    data.split('\n').forEach(line => {
      const trimmed = line.trim();
      if (trimmed === '[ ERROR TABLE ]' || trimmed.startsWith('ERRORS:')) currentSection = 'ERRORS';
      else if (trimmed === '[ SYMBOL TABLE ]' || trimmed.startsWith('SYMBOL TABLE:')) currentSection = 'SYMBOL TABLE';
      else if (trimmed === '[ TYPE TABLE ]' || trimmed.startsWith('TYPE TABLE:')) currentSection = 'TYPE TABLE';
      else if (trimmed === '[ EXPRESSION TABLE ]' || trimmed.startsWith('EXPRESSION TABLE:')) currentSection = 'EXPRESSION TABLE';
      else if (trimmed === '[ CONSTANT TABLE ]') currentSection = 'CONSTANT TABLE';
      else if (trimmed === '[ STRING LITERAL TABLE ]') currentSection = 'STRING TABLE';
      else if (trimmed === '[ TEMPORARY VARIABLE TABLE ]') currentSection = 'TEMP TABLE';
      else if (trimmed && !trimmed.startsWith('=====') && !trimmed.startsWith('[ ') && currentSection) {
        if (currentSection === 'ERRORS') errors.push(trimmed);
        else if (currentSection === 'SYMBOL TABLE') symbolTable.push(trimmed);
        else if (currentSection === 'TYPE TABLE') typeTable.push(trimmed);
        else if (currentSection === 'EXPRESSION TABLE') expressions.push(trimmed);
        else if (currentSection === 'CONSTANT TABLE') constants.push(trimmed);
        else if (currentSection === 'STRING TABLE') strings.push(trimmed);
        else if (currentSection === 'TEMP TABLE') temps.push(trimmed);
      }
    });

    const isEmpty = (arr: string[]) => arr.length === 0 || (arr.length === 1 && (arr[0] === 'Empty' || arr[0] === '{}'));

    return (
      <div className="semantic-grid animated">
        <div className="report-card" style={{ gridColumn: '1 / -1' }}>
          <h4>Errors</h4>
          {errors.length === 1 && errors[0].includes('No') ? (
            <div style={{color: 'var(--success)', fontFamily: 'Fira Code', fontSize: '0.9rem'}}>{errors[0]}</div>
          ) : (
            <ul style={{color: 'var(--error)', paddingLeft: '1rem', fontFamily: 'Fira Code', fontSize: '0.9rem'}}>
              {errors.map((e, i) => <li key={i}>{e}</li>)}
            </ul>
          )}
        </div>
        <div className="report-card">
          <h4>Symbol Table</h4>
          {isEmpty(symbolTable) ? <div style={{color: 'var(--text-secondary)', fontFamily: 'Fira Code', fontSize: '0.9rem'}}>Empty</div> : <div className="raw-output" style={{ color: '#fcd34d' }}>{symbolTable.join('\n')}</div>}
        </div>
        <div className="report-card">
          <h4>Type Table</h4>
          {isEmpty(typeTable) ? <div style={{color: 'var(--text-secondary)', fontFamily: 'Fira Code', fontSize: '0.9rem'}}>Empty</div> : <div className="raw-output" style={{ color: '#38bdf8' }}>{typeTable.join('\n')}</div>}
        </div>
        <div className="report-card">
          <h4>Expression Table</h4>
          {isEmpty(expressions) ? <div style={{color: 'var(--text-secondary)', fontFamily: 'Fira Code', fontSize: '0.9rem'}}>Empty</div> : <div className="raw-output" style={{ color: '#a78bfa' }}>{expressions.join('\n')}</div>}
        </div>
        <div className="report-card">
          <h4>Constant Table</h4>
          {isEmpty(constants) ? <div style={{color: 'var(--text-secondary)', fontFamily: 'Fira Code', fontSize: '0.9rem'}}>Empty</div> : <div className="raw-output" style={{ color: '#fb7185' }}>{constants.join('\n')}</div>}
        </div>
        <div className="report-card">
          <h4>String Literal Table</h4>
          {isEmpty(strings) ? <div style={{color: 'var(--text-secondary)', fontFamily: 'Fira Code', fontSize: '0.9rem'}}>Empty</div> : <div className="raw-output" style={{ color: '#34d399' }}>{strings.join('\n')}</div>}
        </div>
        <div className="report-card">
          <h4>Temp Variable Table</h4>
          {isEmpty(temps) ? <div style={{color: 'var(--text-secondary)', fontFamily: 'Fira Code', fontSize: '0.9rem'}}>Empty</div> : <div className="raw-output" style={{ color: '#c084fc' }}>{temps.join('\n')}</div>}
        </div>
      </div>
    );
  };

  const renderExecution = (data: string) => {
    let outputData = '';
    let traceData = '';
    let memoryData = '';

    let currentSection = '';
    const lines = data.split('\n');

    for (const line of lines) {
      const trimmed = line.trim();
      if (trimmed === '============= OUTPUT =============') {
        currentSection = 'OUTPUT';
        continue;
      } else if (trimmed === '============= TRACE =============') {
        currentSection = 'TRACE';
        continue;
      } else if (trimmed === '============= MEMORY =============') {
        currentSection = 'MEMORY';
        continue;
      }

      if (currentSection === 'OUTPUT') {
        outputData += line + '\n';
      } else if (currentSection === 'TRACE') {
        traceData += line + '\n';
      } else if (currentSection === 'MEMORY') {
        memoryData += line + '\n';
      }
    }

    outputData = outputData.trim();
    traceData = traceData.trim();
    memoryData = memoryData.trim();

    return (
      <div className="semantic-grid animated">
        <div className="report-card" style={{ gridColumn: '1 / -1' }}>
          <h4 style={{ color: '#34d399', marginBottom: '1rem' }}>Program Output</h4>
          <div className="raw-output" style={{ color: '#f8fafc', fontSize: '1.1rem', backgroundColor: '#0f172a', padding: '1rem', borderRadius: '4px' }}>
            {outputData || 'No output'}
          </div>
        </div>
        <div className="report-card">
          <h4 style={{ color: '#fcd34d', marginBottom: '1rem' }}>Execution Trace</h4>
          <div className="raw-output" style={{ color: '#cbd5e1' }}>
            {traceData || 'No trace generated'}
          </div>
        </div>
        <div className="report-card">
          <h4 style={{ color: '#38bdf8', marginBottom: '1rem' }}>Memory State</h4>
          <div className="raw-output" style={{ color: '#93c5fd' }}>
            {memoryData || 'Empty memory'}
          </div>
        </div>
      </div>
    );
  };

  const renderAST = (data: string) => {
    const lines = data.split('\n').filter(l => l.trim() !== '');
    return (
      <div className="code-block animated">
        {lines.map((line, idx) => {
          const match = line.match(/^(\s*)(.*)$/);
          if (!match) return null;
          const [, spaces, content] = match;
          const indentLevel = spaces.length / 2;
          
          const parts = content.split(':');
          const nodeName = parts[0];
          const nodeValue = parts.slice(1).join(':').trim();

          return (
            <div key={idx} className="ast-line" style={{ paddingLeft: `${indentLevel * 20}px` }}>
              {indentLevel > 0 && <span className="ast-indent"></span>}
              <span className="ast-node-name">{nodeName}</span>
              {nodeValue && <span className="ast-node-value">: {nodeValue}</span>}
            </div>
          );
        })}
      </div>
    );
  };

  const renderTAC = (data: any) => {
    let lines: string[] = [];
    if (typeof data === 'string') {
      lines = data.split('\n').filter((l: string) => l.trim() !== '' && !l.includes('====='));
    } else if (Array.isArray(data)) {
      lines = data.map((t: any) => {
        if (t.op === 'IF') return `IF ${t.arg1} GOTO ${t.result}`;
        if (t.op === 'GOTO') return `GOTO ${t.result}`;
        if (t.op === 'LABEL') return `${t.result}:`;
        if (t.op === '=') return `${t.result} = ${t.arg1}`;
        return `${t.result} = ${t.arg1} ${t.op} ${t.arg2}`;
      });
    }

    return (
      <div className="code-block animated">
        {lines.map((line, idx) => {
          // simple syntax highlight
          const safeLine = line.replace(/</g, '&lt;').replace(/>/g, '&gt;');
          const highlighted = safeLine
            .replace(/(=|\+|-|\*|\/|&lt;|&gt;|==|!=|&lt;=|&gt;=)/g, '<span class="code-operator">$1</span>')
            .replace(/\b(t\d+)\b/g, '<span class="code-temp">$1</span>');

          return (
            <div key={idx} className="code-line" dangerouslySetInnerHTML={{ __html: highlighted }} />
          );
        })}
      </div>
    );
  };

  const renderQuadruples = (data: any) => {
    let rows: any[] = [];
    if (typeof data === 'string') {
      const lines = data.split('\n').filter((l: string) => l.trim() !== '' && !l.includes('====='));
      rows = lines.map((line: string) => {
        const match = line.match(/^\s*\(([^,]+),\s*([^,]+),\s*([^,]+),\s*([^)]+)\)\s*$/);
        if (match) {
          return { op: match[1], arg1: match[2], arg2: match[3], result: match[4] };
        }
        return null;
      }).filter(Boolean);
      if (rows.length === 0) return <pre className="raw-output">{data}</pre>;
    } else if (Array.isArray(data)) {
      rows = data;
    } else {
      return <pre className="raw-output">{JSON.stringify(data)}</pre>;
    }

    return (
      <div className="animated">
        <table className="data-table">
          <thead>
            <tr>
              <th>Operator</th>
              <th>Argument 1</th>
              <th>Argument 2</th>
              <th>Result</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row, idx) => (
              <tr key={idx}>
                <td className="code-operator">{row?.op || row?.operator}</td>
                <td className="code-operand">{row?.arg1}</td>
                <td className="code-operand">{row?.arg2}</td>
                <td className="code-temp">{row?.result || row?.res}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  };

  const renderTriples = (data: any) => {
    if (!Array.isArray(data)) return <pre className="raw-output">{typeof data === 'object' ? JSON.stringify(data, null, 2) : data}</pre>;

    return (
      <div className="animated">
        <table className="data-table">
          <thead>
            <tr>
              <th>Index</th>
              <th>Operator</th>
              <th>Argument 1</th>
              <th>Argument 2</th>
            </tr>
          </thead>
          <tbody>
            {data.map((row, idx) => (
              <tr key={idx}>
                <td className="code-temp">({row?.index})</td>
                <td className="code-operator">{row?.operator || row?.op}</td>
                <td className="code-operand">{row?.arg1}</td>
                <td className="code-operand">{row?.arg2}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  };

  const renderIndirectTriples = (data: any) => {
    if (!data || !data.pointerTable || !data.triples) return <pre className="raw-output">{typeof data === 'object' ? JSON.stringify(data, null, 2) : data}</pre>;

    return (
      <div className="animated" style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '2rem', alignItems: 'start' }}>
        <div>
          <h4 style={{ marginBottom: '1rem', color: '#fcd34d' }}>Pointer Table</h4>
          <table className="data-table">
            <thead>
              <tr>
                <th>Statement</th>
                <th>Pointer</th>
              </tr>
            </thead>
            <tbody>
              {data.pointerTable.map((ptr: number, idx: number) => (
                <tr key={idx}>
                  <td className="code-temp">({idx})</td>
                  <td className="code-operand">({ptr})</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div>
          <h4 style={{ marginBottom: '1rem', color: '#fcd34d' }}>Triples Table</h4>
          <table className="data-table">
            <thead>
              <tr>
                <th>Index</th>
                <th>Operator</th>
                <th>Argument 1</th>
                <th>Argument 2</th>
              </tr>
            </thead>
            <tbody>
              {data.triples.map((row: any, idx: number) => (
                <tr key={idx}>
                  <td className="code-temp">({row?.index})</td>
                  <td className="code-operator">{row?.operator || row?.op}</td>
                  <td className="code-operand">{row?.arg1}</td>
                  <td className="code-operand">{row?.arg2}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    );
  };

  const renderOutput = () => {
    if (error) {
      return <div className="output-content">{renderError(error)}</div>;
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

    if (isErrorText(data)) {
      return <div className="output-content">{renderError(data)}</div>;
    }

    if (activeTab === 'lexer' && Array.isArray(data)) {
      return (
        <div className="output-content" style={{ padding: 0 }}>
          <table className="data-table">
            <thead>
              <tr>
                <th>Type</th>
                <th>Value</th>
                <th>Line</th>
              </tr>
            </thead>
            <tbody>
              {data.map((token: Token, idx: number) => (
                <tr key={idx} className="animated" style={{ animationDelay: `${idx * 0.01}s` }}>
                  <td style={{ color: '#34d399', fontWeight: 600 }}>{token.type}</td>
                  <td style={{ color: '#fcd34d' }}>{token.value}</td>
                  <td style={{ color: '#94a3b8' }}>{token.line}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      );
    }

    if (activeTab === 'parser' && typeof data === 'string') {
      return <div className="output-content">{renderAST(data)}</div>;
    }

    if (activeTab === 'semantic' && typeof data === 'string') {
      return <div className="output-content">{renderSemantic(data)}</div>;
    }

    if (activeTab === 'tac' && data) {
      return <div className="output-content">{renderTAC(data)}</div>;
    }

    if (activeTab === 'quadruple' && data) {
      return <div className="output-content" style={{ padding: 0 }}>{renderQuadruples(data)}</div>;
    }

    if (activeTab === 'triples' && data) {
      return <div className="output-content" style={{ padding: 0 }}>{renderTriples(data)}</div>;
    }

    if (activeTab === 'indirectTriples' && data) {
      return <div className="output-content" style={{ padding: 0 }}>{renderIndirectTriples(data)}</div>;
    }

    if (activeTab === 'target' && data) {
      let lines: string[] = [];
      if (typeof data === 'string') {
        lines = data.split('\n').filter((l: string) => l.trim() !== '' && !l.includes('====='));
      } else if (Array.isArray(data)) {
        lines = data;
      }

      if (lines.length > 0) {
        return (
          <div className="output-content">
            <div className="code-block animated">
              {lines.map((line, idx) => (
                <div key={idx} className="code-line">{line}</div>
              ))}
            </div>
          </div>
        );
      }
    }

    if (activeTab === 'execution' && typeof data === 'string') {
      return (
        <div className="output-content" style={{ padding: 0 }}>
          {renderExecution(data)}
        </div>
      );
    }

    if (activeTab === 'full' && typeof data === 'object') {
      return (
        <div className="output-content animated">
          <div className="report-card" style={{ gridColumn: '1 / -1' }}>
            <h3>Semantic Analysis</h3>
            {renderSemantic(data.semantic || '')}
          </div>
          <div className="report-card">
            <h3>Three Address Code (TAC)</h3>
            {renderTAC(data.tac || '')}
          </div>
          <div className="report-card">
            <h3>Quadruples</h3>
            {renderQuadruples(data.quadruples || [])}
          </div>
          <div className="report-card">
            <h3>Triples</h3>
            {renderTriples(data.triples || [])}
          </div>
          <div className="report-card" style={{ gridColumn: '1 / -1' }}>
            <h3>Indirect Triples</h3>
            {renderIndirectTriples(data.indirectTriples || {})}
          </div>
          <div className="report-card">
            <h3>Optimized Quadruples</h3>
            {renderQuadruples(data.optimized || [])}
          </div>
        </div>
      );
    }

    return (
      <div className="output-content animated">
        <pre className="raw-output">
          {typeof data === 'string' ? data : JSON.stringify(data, null, 2)}
        </pre>
      </div>
    );
  };

  const tabs: { id: TabType, label: string }[] = [
    { id: 'lexer', label: 'Tokens' },
    { id: 'parser', label: 'AST' },
    { id: 'semantic', label: 'Semantic' },
    { id: 'tac', label: 'TAC' },
    { id: 'quadruple', label: 'Quadruple' },
    { id: 'triples', label: 'Triples' },
    { id: 'indirectTriples', label: 'Indirect Triples' },
    { id: 'target', label: 'Target Code' },
    { id: 'execution', label: 'Execution Trace' },
    { id: 'full', label: 'Full Report' },
  ];

  return (
    <div className="app-container">
      <header className="header">
        <div className="logo">
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
