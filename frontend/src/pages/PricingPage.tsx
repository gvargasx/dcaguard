import { Link, useNavigate } from 'react-router-dom';
import { Shield, Check, X, ArrowLeft, Zap, Crown, Star } from 'lucide-react';
import { signInAnonymously, signInWithPopup, GoogleAuthProvider } from 'firebase/auth';
import { auth } from '../firebase';
import { useAuth } from '../context/AuthContext';
import { cn } from '../utils/format';
import { useState } from 'react';

const googleProvider = new GoogleAuthProvider();
googleProvider.setCustomParameters({ prompt: 'select_account' });

const PLANS = [
  {
    name: 'Anonymous',
    price: 'Free',
    desc: 'Try it out, no account needed',
    icon: Zap,
    color: 'surface-400',
    action: 'anonymous',
    features: [
      { text: '1 portfolio', included: true },
      { text: '2 DCA plans', included: true },
      { text: '7-day history', included: true },
      { text: 'Basic risk score', included: true },
      { text: 'Contains ads', included: false },
      { text: 'CSV/PDF export', included: false },
      { text: 'Advanced alerts', included: false },
    ],
  },
  {
    name: 'Free',
    price: 'Free',
    desc: 'Sign in for more power',
    icon: Star,
    color: 'brand-400',
    action: 'google',
    features: [
      { text: '3 portfolios', included: true },
      { text: '10 DCA plans', included: true },
      { text: '90-day history', included: true },
      { text: 'Full risk analysis', included: true },
      { text: 'Contains ads', included: false },
      { text: 'CSV/PDF export', included: false },
      { text: 'Advanced alerts', included: false },
    ],
  },
  {
    name: 'Pro',
    price: '$4.99/mo',
    desc: 'Unlimited everything',
    icon: Crown,
    color: 'accent-amber',
    popular: true,
    action: 'google',
    features: [
      { text: 'Unlimited portfolios', included: true },
      { text: 'Unlimited DCA plans', included: true },
      { text: '365-day history', included: true },
      { text: 'Full risk analysis', included: true },
      { text: 'No ads', included: true },
      { text: 'CSV/PDF export', included: true },
      { text: 'Advanced alerts', included: true },
    ],
  },
];

export default function PricingPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [loading, setLoading] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleAction = async (plan: typeof PLANS[0]) => {
    if (user) {
      navigate('/app');
      return;
    }

    setLoading(plan.name);
    setError(null);
    try {
      if (plan.action === 'anonymous') {
        await signInAnonymously(auth);
      } else {
        await signInWithPopup(auth, googleProvider);
      }
      navigate('/app');
    } catch (err: any) {
      console.error('Auth error:', err);
      setError(err.message || 'Authentication failed');
    } finally {
      setLoading(null);
    }
  };

  return (
    <div className="min-h-screen bg-surface-950 noise-bg">
      <nav className="max-w-7xl mx-auto px-6 py-5 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-lg bg-brand-500/15 flex items-center justify-center">
            <Shield className="w-4 h-4 text-brand-400" />
          </div>
          <span className="font-display font-bold text-sm tracking-tight">DCA GUARD</span>
        </Link>
        <Link to="/app" className="btn-ghost text-xs flex items-center gap-1">
          <ArrowLeft className="w-3.5 h-3.5" /> Back to App
        </Link>
      </nav>

      <section className="max-w-5xl mx-auto px-6 pt-16 pb-24 text-center">
        <h1 className="text-3xl md:text-4xl font-display font-bold mb-3">Simple, transparent pricing</h1>
        <p className="text-surface-400 text-lg mb-12">Start free, upgrade when you need more.</p>

        {error && (
          <div className="max-w-md mx-auto mb-6 p-3 rounded-xl bg-accent-rose/10 border border-accent-rose/30 text-xs text-accent-rose">
            {error}
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
          {PLANS.map((plan) => {
            const Icon = plan.icon;
            const isLoading = loading === plan.name;
            return (
              <div
                key={plan.name}
                className={cn(
                  'card p-6 text-left relative',
                  plan.popular && 'border-brand-500/50 glow-green'
                )}
              >
                {plan.popular && (
                  <div className="absolute -top-3 left-1/2 -translate-x-1/2">
                    <span className="badge-green text-[10px] px-3 py-1">Most Popular</span>
                  </div>
                )}

                <div className={`w-10 h-10 rounded-xl bg-${plan.color}/10 flex items-center justify-center mb-4`}>
                  <Icon className={`w-5 h-5 text-${plan.color}`} />
                </div>

                <h3 className="text-lg font-semibold mb-1">{plan.name}</h3>
                <p className="text-xs text-surface-500 mb-4">{plan.desc}</p>

                <p className="text-2xl font-display font-bold mb-6">
                  {plan.price}
                  {plan.price !== 'Free' && <span className="text-xs text-surface-500 font-normal"> /month</span>}
                </p>

                <div className="space-y-3 mb-6">
                  {plan.features.map((f, i) => (
                    <div key={i} className="flex items-center gap-2 text-xs">
                      {f.included ? (
                        <Check className="w-4 h-4 text-brand-400" />
                      ) : (
                        <X className="w-4 h-4 text-surface-600" />
                      )}
                      <span className={f.included ? 'text-surface-200' : 'text-surface-500'}>{f.text}</span>
                    </div>
                  ))}
                </div>

                <button
                  onClick={() => handleAction(plan)}
                  disabled={isLoading}
                  className={cn(
                    'w-full py-2.5 rounded-xl text-sm font-medium transition-all',
                    plan.popular ? 'btn-primary' : 'btn-secondary'
                  )}
                >
                  {isLoading
                    ? 'Connecting...'
                    : user
                      ? 'Go to Dashboard'
                      : plan.name === 'Pro'
                        ? 'Start Pro Trial'
                        : plan.name === 'Free'
                          ? 'Sign In with Google'
                          : 'Get Started'}
                </button>
              </div>
            );
          })}
        </div>
      </section>
    </div>
  );
}