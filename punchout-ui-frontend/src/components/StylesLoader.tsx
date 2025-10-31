'use client';

import { useEffect } from 'react';

const StylesLoader = () => {
  useEffect(() => {
    // Import CSS files on the client side
    import('bootstrap/dist/css/bootstrap.min.css');
    import('@fortawesome/fontawesome-free/css/all.min.css');
  }, []);

  return null;
};

export default StylesLoader;