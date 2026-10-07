import { useState, useEffect } from 'react';

interface SearchBoxProps {
    onSearch: (keyword: string) => void;
    placeholder?: string;
}

export default function SearchBox({ onSearch, placeholder }: SearchBoxProps) {
    const [inputValue, setInputValue] = useState('');

    // debounce 400ms: chỉ gọi API khi người dùng ngừng gõ
    useEffect(() => {
        const timer = setTimeout(() => onSearch(inputValue.trim()), 400);
        return () => clearTimeout(timer);
    }, [inputValue, onSearch]);

    return (
        <input
            type="text"
            className="search-box"
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            placeholder={placeholder ?? 'Tìm theo tên sách hoặc tác giả...'}
        />
    );
}
