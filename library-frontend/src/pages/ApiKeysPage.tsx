import { useState, type FormEvent } from 'react';
import { getApiKeys, createApiKey, revokeApiKey } from '../api/apiKeyApi';
import { useFetch } from '../hooks/useFetch';
import { getErrorMessage } from '../utils/errors';
import { formatDate } from '../utils/format';
import type { ApiKey } from '../types/apiKey';

const fetchApiKeys = () => getApiKeys().then((res) => res.data);

export default function ApiKeysPage() {
    const { data, error: loadError, loading, reload } = useFetch(fetchApiKeys, 'Không tải được danh sách API Key.');
    const keys: ApiKey[] = data ?? [];
    const [ownerName, setOwnerName] = useState('');
    const [scopes, setScopes] = useState('books:read');
    const [validDays, setValidDays] = useState('30');
    const [newKeyValue, setNewKeyValue] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);

    const handleCreate = async (e: FormEvent) => {
        e.preventDefault();
        setError(null);
        setNewKeyValue(null);
        try {
            const res = await createApiKey({
                ownerName,
                scopes,
                validDays: validDays ? Number(validDays) : undefined,
            });
            setNewKeyValue(res.data.keyValue);
            setOwnerName('');
            reload();
        } catch (err) {
            setError(getErrorMessage(err, 'Cấp API Key không thành công.'));
        }
    };

    const handleRevoke = async (key: ApiKey) => {
        if (!window.confirm(`Thu hồi API Key của "${key.ownerName}"?`)) return;
        try {
            await revokeApiKey(key.id);
            reload();
        } catch {
            alert('Thu hồi không thành công.');
        }
    };

    return (
        <div className="page">
            <h1>Quản lý API Key đối tác</h1>
            <p className="muted">
                Đối tác gọi <code>GET /api/public/books</code> kèm header <code>X-API-KEY</code> (scope <code>books:read</code>).
            </p>

            <form onSubmit={handleCreate} className="card form-grid">
                <h3>Cấp API Key mới</h3>
                <label>Tên đối tác
                    <input value={ownerName} onChange={(e) => setOwnerName(e.target.value)} required />
                </label>
                <label>Scopes (cách nhau bởi dấu phẩy)
                    <input value={scopes} onChange={(e) => setScopes(e.target.value)} required />
                </label>
                <label>Hiệu lực (số ngày, để trống = vĩnh viễn)
                    <input type="number" value={validDays} onChange={(e) => setValidDays(e.target.value)} />
                </label>
                {(error ?? loadError) && <p className="field-error full">{error ?? loadError}</p>}
                <div className="full"><button type="submit" className="primary">Cấp API Key</button></div>
            </form>

            {newKeyValue && (
                <div className="notice">
                    <strong>Key vừa tạo (hãy lưu lại ngay):</strong>
                    <pre>{newKeyValue}</pre>
                </div>
            )}

            {loading && !data ? <p className="muted">Đang tải...</p> : (
                <div className="table-wrap">
                    <table>
                        <thead>
                            <tr><th>Đối tác</th><th>Scopes</th><th>Trạng thái</th><th>Hết hạn</th><th>Thao tác</th></tr>
                        </thead>
                        <tbody>
                            {keys.map((k) => (
                                <tr key={k.id}>
                                    <td>{k.ownerName}</td>
                                    <td>{k.scopes}</td>
                                    <td>
                                        <span className={`badge ${k.status === 'ACTIVE' ? 'badge-ok' : 'badge-danger'}`}>
                                            {k.status === 'ACTIVE' ? 'Đang hiệu lực' : 'Đã thu hồi'}
                                        </span>
                                    </td>
                                    <td>{k.expiresAt ? formatDate(k.expiresAt) : 'Vĩnh viễn'}</td>
                                    <td>
                                        {k.status === 'ACTIVE' && (
                                            <button className="danger" onClick={() => handleRevoke(k)}>Thu hồi</button>
                                        )}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}
