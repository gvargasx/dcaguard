import { useEffect, useRef, useState } from 'react';
import { useAuth } from '../../context/AuthContext';

// ============================================================
// SETUP: Replace placeholder IDs when your AdSense is approved
// 1. index.html: ca-pub-XXXXXXXXXXXXXXXX → your publisher ID
// 2. Below: AD_CLIENT → your publisher ID
// 3. Below: slot values in AD_SLOTS → your ad unit IDs
// ============================================================
const AD_CLIENT = 'ca-pub-2890319082813523';
const IS_ADSENSE_CONFIGURED = false;

const AD_SLOTS: Record<string, { slot: string; format: string; style: React.CSSProperties }> = {
  banner: {
    slot: '1234567890',
    format: 'horizontal',
    style: { display: 'block', width: '100%', height: '90px' },
  },
  inline: {
    slot: '0987654321',
    format: 'rectangle',
    style: { display: 'block', width: '100%', height: '250px' },
  },
  sidebar: {
    slot: '1122334455',
    format: 'vertical',
    style: { display: 'block', width: '100%', height: '160px' },
  },
};

declare global {
  interface Window {
    adsbygoogle: any[];
  }
}

interface AdBannerProps {
  placement?: 'inline' | 'sidebar' | 'banner';
}

export default function AdBanner({ placement = 'inline' }: AdBannerProps) {
  const { user } = useAuth();
  const pushed = useRef(false);

  // Don't show ads to Pro users
  if (user && !user.hasAds) return null;

  const config = AD_SLOTS[placement] || AD_SLOTS.inline;

  useEffect(() => {
    if (!IS_ADSENSE_CONFIGURED || pushed.current) return;
    try {
      window.adsbygoogle = window.adsbygoogle || [];
      window.adsbygoogle.push({});
      pushed.current = true;
    } catch (e) {
      console.debug('AdSense push error:', e);
    }
    return () => { pushed.current = false; };
  }, []);

  // Show real AdSense if configured
  if (IS_ADSENSE_CONFIGURED) {
    return (
      <div className="relative rounded-xl overflow-hidden bg-surface-800/20 border border-surface-700/30">
        <ins
          className="adsbygoogle"
          style={config.style}
          data-ad-client={AD_CLIENT}
          data-ad-slot={config.slot}
          data-ad-format={config.format}
          data-full-width-responsive="true"
        />
      </div>
    );
  }

  // Fallback: styled placeholder
  return <AdPlaceholder placement={placement} />;
}

function AdPlaceholder({ placement }: { placement: string }) {
  const [hovering, setHovering] = useState(false);

  const heights: Record<string, string> = {
    banner: 'h-[72px]',
    inline: 'h-[200px]',
    sidebar: 'h-[140px]',
  };

  const ads = [
    { title: 'Trade Smarter', subtitle: 'Low fees, fast execution', brand: 'CryptoExchange', color: '#22c55e' },
    { title: 'Secure Your Portfolio', subtitle: 'Hardware wallet protection', brand: 'SafeVault', color: '#06b6d4' },
    { title: 'DeFi Yields up to 12%', subtitle: 'Stake & earn passively', brand: 'YieldFi', color: '#f59e0b' },
    { title: 'Zero-Fee Trading', subtitle: 'Start with $10', brand: 'EasyTrade', color: '#8b5cf6' },
  ];

  const ad = ads[Math.floor(Math.random() * ads.length)];

  return (
    <div
      className={`${heights[placement] || heights.banner} w-full rounded-xl border border-surface-700/30 flex items-center justify-center relative overflow-hidden transition-all duration-300`}
      style={{
        background: `linear-gradient(135deg, ${ad.color}08 0%, transparent 60%)`,
        borderColor: hovering ? `${ad.color}30` : undefined,
      }}
      onMouseEnter={() => setHovering(true)}
      onMouseLeave={() => setHovering(false)}
    >
      {/* Ad label */}
      <span className="absolute top-1.5 left-2.5 text-[8px] uppercase tracking-widest text-surface-600 font-medium">
        Ad
      </span>

      <div className="flex items-center gap-4 px-6">
        {/* Brand icon */}
        <div
          className="w-9 h-9 rounded-lg flex items-center justify-center text-xs font-bold shrink-0"
          style={{ backgroundColor: `${ad.color}15`, color: ad.color }}
        >
          {ad.brand.charAt(0)}
        </div>

        <div className="min-w-0">
          <p className="text-sm font-semibold text-surface-200 truncate">{ad.title}</p>
          <p className="text-[11px] text-surface-500 truncate">{ad.subtitle}</p>
        </div>

        {placement !== 'sidebar' && (
          <div
            className="ml-auto px-3 py-1.5 rounded-lg text-[10px] font-semibold shrink-0 transition-colors"
            style={{ backgroundColor: `${ad.color}15`, color: ad.color }}
          >
            Learn More
          </div>
        )}
      </div>
    </div>
  );
}