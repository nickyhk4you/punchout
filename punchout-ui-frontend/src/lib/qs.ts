export const buildParams = (obj?: Record<string, string | number | boolean | undefined | null>): URLSearchParams => {
  const params = new URLSearchParams();
  
  if (!obj) return params;
  
  Object.entries(obj).forEach(([key, value]) => {
    if (value !== undefined && value !== null && `${value}` !== '') {
      params.append(key, String(value));
    }
  });
  
  return params;
};
