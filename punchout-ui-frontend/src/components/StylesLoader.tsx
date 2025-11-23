'use client';

import { useEffect } from 'react';

const StylesLoader = () => {
  useEffect(() => {
    // Import CSS files on the client side
    // @ts-ignore - CSS imports
    import('bootstrap/dist/css/bootstrap.min.css').catch(() => {});
    // @ts-ignore - CSS imports
    import('@fortawesome/fontawesome-free/css/all.min.css').catch(() => {});
  }, []);

  return null;
};

export default StylesLoader;