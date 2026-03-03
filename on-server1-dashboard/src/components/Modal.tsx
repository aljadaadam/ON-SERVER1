import { useEffect, useCallback, useRef } from 'react';
import { createPortal } from 'react-dom';
import { XMarkIcon } from '@heroicons/react/24/outline';

interface ModalProps {
  open: boolean;
  onClose: () => void;
  children: React.ReactNode;
  title?: string;
  icon?: React.ReactNode;
  size?: 'sm' | 'md' | 'lg' | 'xl';
  /** Hide the X close button */
  hideClose?: boolean;
}

const sizeClasses: Record<string, string> = {
  sm: 'max-w-sm',
  md: 'max-w-lg',
  lg: 'max-w-2xl',
  xl: 'max-w-4xl',
};

export default function Modal({ open, onClose, children, title, icon, size = 'md', hideClose }: ModalProps) {
  const overlayRef = useRef<HTMLDivElement>(null);

  // Close on Escape
  const handleKey = useCallback((e: KeyboardEvent) => {
    if (e.key === 'Escape') onClose();
  }, [onClose]);

  useEffect(() => {
    if (!open) return;
    document.addEventListener('keydown', handleKey);
    // Lock body + main scroll
    const prev = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    const mainEl = document.querySelector('main');
    const prevMain = mainEl?.style.overflow || '';
    if (mainEl) mainEl.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', handleKey);
      document.body.style.overflow = prev;
      if (mainEl) mainEl.style.overflow = prevMain;
    };
  }, [open, handleKey]);

  if (!open) return null;

  return createPortal(
    <div
      ref={overlayRef}
      className="fixed inset-0 flex items-center justify-center p-4 animate-modal-overlay"
      style={{ zIndex: 9999 }}
      onClick={(e) => { if (e.target === overlayRef.current) onClose(); }}
    >
      {/* Backdrop */}
      <div className="fixed inset-0 bg-black/50 backdrop-blur-sm pointer-events-none" />

      {/* Panel */}
      <div className={`relative z-10 bg-white dark:bg-dark-card rounded-2xl shadow-2xl w-full ${sizeClasses[size]} max-h-[90vh] overflow-y-auto animate-modal-panel`}>
        {/* Header */}
        {(title || !hideClose) && (
          <div className="sticky top-0 z-10 flex items-center justify-between px-6 pt-5 pb-3 bg-white/95 dark:bg-dark-card/95 backdrop-blur-sm rounded-t-2xl">
            {title && (
              <h3 className="text-lg font-bold text-gray-900 dark:text-white flex items-center gap-2">
                {icon}
                {title}
              </h3>
            )}
            {!title && <div />}
            {!hideClose && (
              <button
                onClick={onClose}
                className="p-1.5 rounded-xl text-gray-400 hover:text-gray-700 dark:hover:text-white hover:bg-gray-100 dark:hover:bg-dark-surface transition-all duration-200"
              >
                <XMarkIcon className="w-5 h-5" />
              </button>
            )}
          </div>
        )}

        {/* Body */}
        <div className="px-6 pb-6">
          {children}
        </div>
      </div>
    </div>,
    document.body
  );
}
