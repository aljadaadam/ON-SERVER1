import prisma from '../config/database';

// ============================================
// DHRU FUSION API Integration
// ============================================

let puppeteer: any = null;
try {
  puppeteer = require('puppeteer');
} catch {
  console.log('[SD-Unlocker] puppeteer not installed — Cloudflare bypass unavailable');
}

/** Custom field definition from SD-Unlocker service */
export interface ServiceField {
  name: string;    // Display name
  key: string;     // Field identifier
  type: string;    // "text", "textarea", etc.
  required: boolean;
}

/** Parsed service from SD-Unlocker API */
export interface SDService {
  serviceId: string;
  serviceName: string;
  credit: number;         // Cost price
  serviceType: string;    // IMEI, SERVER, REMOTE
  supportsQnt: boolean;
  minQnt: number;
  maxQnt: number;
  deliveryTime: string;
  groupName: string;
  fields: ServiceField[];
}

export interface PlaceOrderResult {
  success: boolean;
  referenceId: string;
  message?: string;
  rawResponse?: any;
}

export interface OrderStatusResult {
  success: boolean;
  status: string;       // PENDING, PROCESSING, COMPLETED, REJECTED, etc.
  codes?: string;       // Result codes/keys returned
  message?: string;
  rawResponse?: any;
}

export interface BalanceResult {
  success: boolean;
  balance: number;
  message?: string;
}

/**
 * SD-Unlocker External Provider Service
 * API: https://sd-unlocker.com/api/index.php
 * Format: POST with application/x-www-form-urlencoded
 * Actions: imeiservicelist, placeimeiorder, getimeiorder, getbalance
 */
export class ExternalProviderService {
  private apiUrl: string;
  private username: string;
  private apiKey: string;

  constructor() {
    this.apiUrl = '';
    this.username = '';
    this.apiKey = '';
  }

  /** Reload config from database settings */
  async reloadConfig(): Promise<void> {
    try {
      const settings = await prisma.setting.findMany({
        where: { key: { in: ['provider_url', 'provider_username', 'provider_api_key'] } },
      });
      const map = new Map(settings.map(s => [s.key, s.value]));
      this.apiUrl = map.get('provider_url') || '';
      this.username = map.get('provider_username') || '';
      this.apiKey = map.get('provider_api_key') || '';
    } catch (err) {
      console.warn('[Provider] Failed to reload config from DB');
    }
  }

  /** Get current provider settings */
  async getProviderSettings(): Promise<{ url: string; username: string; apiKey: string }> {
    await this.reloadConfig();
    return {
      url: this.apiUrl,
      username: this.username,
      apiKey: this.apiKey ? '••••' + this.apiKey.slice(-6) : '',
    };
  }

  /** Update provider settings in DB */
  async updateProviderSettings(data: { url?: string; username?: string; apiKey?: string }): Promise<void> {
    const entries: { key: string; value: string }[] = [];
    if (data.url) entries.push({ key: 'provider_url', value: data.url });
    if (data.username) entries.push({ key: 'provider_username', value: data.username });
    if (data.apiKey) entries.push({ key: 'provider_api_key', value: data.apiKey });
    for (const { key, value } of entries) {
      await prisma.setting.upsert({ where: { key }, update: { value }, create: { key, value } });
    }
    await this.reloadConfig();
  }

  /** Build full API endpoint URL from stored domain */
  private getFullApiUrl(): string {
    if (!this.apiUrl) return '';
    const domain = this.apiUrl.replace(/\/+$/, ''); // remove trailing slashes
    return `${domain}/api/index.php`;
  }

  /** Build common form params for all SD-Unlocker requests */
  private buildParams(action: string, xmlParameters?: string): URLSearchParams {
    const params = new URLSearchParams();
    params.append('username', this.username);
    params.append('apiaccesskey', this.apiKey);
    params.append('requestformat', 'JSON');
    params.append('action', action);
    if (xmlParameters) {
      params.append('parameters', xmlParameters);
    }
    return params;
  }

  /** Make a POST request to Provider API */
  private async apiRequest(action: string, xmlParameters?: string): Promise<any> {
    // Reload config from DB before each API call
    await this.reloadConfig();
    const params = this.buildParams(action, xmlParameters);
    
    // Try fetch first (faster), fallback to Puppeteer if Cloudflare blocked
    try {
      return await this.fetchRequest(params);
    } catch (fetchErr: any) {
      if (fetchErr.message.includes('Cloudflare') || fetchErr.message.includes('403')) {
        console.log(`[Provider] fetch blocked by Cloudflare, switching to Puppeteer browser...`);
        return await this.puppeteerRequest(params);
      }
      throw fetchErr;
    }
  }

  /** Standard fetch-based request */
  private async fetchRequest(params: URLSearchParams): Promise<any> {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 90_000);

    const headers: Record<string, string> = {
      'Content-Type': 'application/x-www-form-urlencoded',
      'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
      'Accept': 'application/json, text/plain, */*',
      'Accept-Language': 'en-US,en;q=0.9',
      'Accept-Encoding': 'gzip, deflate, br',
      'Origin': 'https://sd-unlocker.com',
      'Referer': 'https://sd-unlocker.com/',
    };

    let response: Response;
    try {
      response = await fetch(this.getFullApiUrl(), {
        method: 'POST',
        headers,
        body: params.toString(),
        signal: controller.signal,
      });
    } catch (err: any) {
      clearTimeout(timeout);
      if (err.name === 'AbortError') {
        throw new Error('SD-Unlocker API request timed out (90s)');
      }
      throw new Error(`SD-Unlocker API connection failed: ${err.message}`);
    } finally {
      clearTimeout(timeout);
    }

    if (response.status === 403) {
      throw new Error('SD-Unlocker API blocked by Cloudflare (403)');
    }

    if (!response.ok) {
      throw new Error(`SD-Unlocker API HTTP error: ${response.status}`);
    }

    const text = await response.text();
    try {
      return JSON.parse(text);
    } catch {
      throw new Error(`SD-Unlocker API returned non-JSON: ${text.substring(0, 300)}`);
    }
  }

  /** Puppeteer-based request — launches real browser to bypass Cloudflare JS challenge */
  private async puppeteerRequest(params: URLSearchParams): Promise<any> {
    if (!puppeteer) {
      throw new Error('Puppeteer not installed. Run: npm install puppeteer');
    }

    console.log('[SD-Unlocker] Launching headless browser...');
    const browser = await puppeteer.launch({
      headless: 'new',
      args: [
        '--no-sandbox',
        '--disable-setuid-sandbox',
        '--disable-dev-shm-usage',
        '--disable-gpu',
      ],
    });

    try {
      const page = await browser.newPage();
      
      // Set a realistic user-agent
      await page.setUserAgent(
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
      );

      // First navigate to homepage to pass Cloudflare challenge
      console.log('[SD-Unlocker] Navigating to homepage to pass Cloudflare...');
      await page.goto('https://sd-unlocker.com', { 
        waitUntil: 'networkidle2', 
        timeout: 60_000 
      });

      // Wait for Cloudflare challenge to resolve (up to 30s)
      await page.waitForFunction(
        () => !document.title.includes('Just a moment'),
        { timeout: 30_000 }
      ).catch(() => {
        console.log('[SD-Unlocker] Cloudflare challenge may not have resolved, trying API anyway...');
      });

      console.log('[SD-Unlocker] Cloudflare passed, sending API request...');

      // Now make the actual API request using page.evaluate (keeps cookies/session)
      const result = await page.evaluate(async (apiUrl: string, body: string) => {
        const resp = await fetch(apiUrl, {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body,
        });
        if (!resp.ok) {
          return { __error: true, status: resp.status, text: await resp.text().catch(() => '') };
        }
        const text = await resp.text();
        try {
          return JSON.parse(text);
        } catch {
          return { __error: true, status: resp.status, text: text.substring(0, 500) };
        }
      }, this.getFullApiUrl(), params.toString());

      if (result && result.__error) {
        throw new Error(`SD-Unlocker API error via Puppeteer: HTTP ${result.status} — ${(result.text || '').substring(0, 200)}`);
      }

      console.log('[SD-Unlocker] Puppeteer request successful!');
      return result;
    } finally {
      await browser.close();
      console.log('[SD-Unlocker] Browser closed');
    }
  }

  /** Generate random 15-digit IMEI for SERVER type orders */
  generateRandomImei(): string {
    let imei = '';
    for (let i = 0; i < 15; i++) {
      imei += Math.floor(Math.random() * 10).toString();
    }
    return imei;
  }

  // ============================================
  // 1. Fetch Service List (imeiservicelist)
  // ============================================
  async fetchServiceList(): Promise<SDService[]> {
    try {
      const data = await this.apiRequest('imeiservicelist');
      
      if (data.ERROR) {
        const errMsg = Array.isArray(data.ERROR) ? data.ERROR[0]?.MESSAGE : data.ERROR;
        throw new Error(`SD-Unlocker error: ${errMsg}`);
      }

      if (!data.SUCCESS) {
        throw new Error('SD-Unlocker: No SUCCESS data in service list response');
      }

      const services: SDService[] = [];
      const successData = Array.isArray(data.SUCCESS) ? data.SUCCESS : [data.SUCCESS];

      // Actual response: SUCCESS[0].LIST = { "GroupName": { GROUPNAME, SERVICES: { "id": {...} } } }
      for (const successItem of successData) {
        const list = successItem.LIST || successItem;
        
        // LIST is an object where keys are group names
        const groups = typeof list === 'object' && !Array.isArray(list) 
          ? Object.values(list) 
          : (Array.isArray(list) ? list : [list]);

        for (const group of groups) {
          if (!group || typeof group !== 'object') continue;
          const groupName = group.GROUPNAME || 'Unknown';
          
          // SERVICES is an object { "20219": {...}, "20220": {...} } not an array
          let serviceEntries: any[] = [];
          if (group.SERVICES && typeof group.SERVICES === 'object' && !Array.isArray(group.SERVICES)) {
            serviceEntries = Object.values(group.SERVICES);
          } else if (Array.isArray(group.SERVICES)) {
            serviceEntries = group.SERVICES;
          }

          for (const svc of serviceEntries) {
            if (!svc || typeof svc !== 'object') continue;
            
            // Parse custom fields from all possible locations in the service data
            const fields: ServiceField[] = [];
            const customObj = svc.CUSTOM || svc.custom || svc.Custom;
            // Check multiple possible key names for custom field requirements
            const reqCustom = svc['Requires.Custom'] || svc['RequiresCustom'] || svc['REQUIRESCUSTOM']
              || svc['requires.custom'] || svc['REQCUST'] || svc['reqcust']
              || (svc.Requires && typeof svc.Requires === 'object' ? (svc.Requires.Custom || svc.Requires.CUSTOM || svc.Requires.custom) : undefined)
              || (svc.REQUIRES && typeof svc.REQUIRES === 'object' ? (svc.REQUIRES.Custom || svc.REQUIRES.CUSTOM || svc.REQUIRES.custom) : undefined);

            // Helper: extract a field from a field-like object
            const extractField = (cf: any): ServiceField | null => {
              if (!cf || typeof cf !== 'object') return null;
              if (cf.adminonly === 'on' || cf.ADMINONLY === 'on') return null;
              const fieldName = (
                cf.fieldname || cf.FIELDNAME || cf.FieldName
                || cf.customname || cf.CUSTOMNAME || cf.CustomName
                || cf.name || cf.NAME || cf.Name
                || cf.label || cf.LABEL || cf.Label
                || cf.title || cf.TITLE || ''
              ).toString().trim();
              if (!fieldName) return null;
              const fieldType = (
                cf.fieldtype || cf.FIELDTYPE || cf.FieldType
                || cf.type || cf.TYPE || cf.Type || 'text'
              ).toString().toUpperCase();
              const isRequired = cf.required === 'on' || cf.required === true || cf.required === '1'
                || cf.REQUIRED === 'on' || cf.REQUIRED === true || cf.REQUIRED === '1';
              return { name: fieldName, key: fieldName, type: fieldType, required: isRequired || true };
            };

            // Format 1a: CUSTOM as single-field object { allow:'1', customname, custominfo, isalpha, ... }
            if (customObj && typeof customObj === 'object' && !Array.isArray(customObj)) {
              const allow = customObj.allow || customObj.ALLOW || customObj.Allow;
              const cName = (customObj.customname || customObj.CUSTOMNAME || customObj.CustomName || '').toString().trim();
              if (allow === '1' || allow === 1 || allow === true || allow === 'yes' || allow === 'YES') {
                if (cName) {
                  const isAlpha = customObj.isalpha || customObj.ISALPHA || customObj.IsAlpha;
                  fields.push({
                    name: cName,
                    key: cName,
                    type: (isAlpha === '1' || isAlpha === 1) ? 'TEXT' : 'NUMBER',
                    required: true,
                  });
                }
              }
            }

            // Format 1b: CUSTOM as array of field objects [{ fieldname, fieldtype, ... }]
            if (Array.isArray(customObj) && fields.length === 0) {
              for (const cf of customObj) {
                const f = extractField(cf);
                if (f) fields.push(f);
              }
            }

            // Format 1c: CUSTOM as object with numbered/named keys { "1": { fieldname, ... }, "2": { ... } }
            if (customObj && typeof customObj === 'object' && !Array.isArray(customObj) && fields.length === 0) {
              const customValues = Object.values(customObj);
              if (customValues.length > 0 && customValues.some((v: any) => v && typeof v === 'object' && (v.fieldname || v.FIELDNAME || v.customname || v.CUSTOMNAME))) {
                for (const cf of customValues as any[]) {
                  const f = extractField(cf);
                  if (f) fields.push(f);
                }
              }
            }

            // Format 2: Requires.Custom array [{fieldname, fieldtype, required, ...}, ...]
            if (Array.isArray(reqCustom) && fields.length === 0) {
              for (const cf of reqCustom) {
                const f = extractField(cf);
                if (f) fields.push(f);
              }
            } else if (typeof reqCustom === 'string' && reqCustom.length > 0 && fields.length === 0) {
              // Format 3: Requires.Custom as JSON string
              try {
                const parsed = JSON.parse(reqCustom);
                if (Array.isArray(parsed)) {
                  for (const cf of parsed) {
                    const f = extractField(cf);
                    if (f) fields.push(f);
                  }
                } else if (parsed && typeof parsed === 'object') {
                  // Single field as JSON string
                  const f = extractField(parsed);
                  if (f) fields.push(f);
                }
              } catch {}
            }

            // Format 4: CUSTOMLIST array
            const customList = svc.CUSTOMLIST || svc.CustomList || svc.customlist || svc.CUSTOM_LIST;
            if (Array.isArray(customList) && fields.length === 0) {
              for (const cf of customList) {
                const f = extractField(cf);
                if (f) fields.push(f);
              }
            }

            // Format 5: CUSTOMFIELDS as object with entries
            const customFieldsObj = svc.CUSTOMFIELDS || svc.CustomFields || svc.customfields || svc.CUSTOM_FIELDS;
            if (customFieldsObj && typeof customFieldsObj === 'object' && !Array.isArray(customFieldsObj) && fields.length === 0) {
              const cfValues = Object.values(customFieldsObj) as any[];
              for (const cf of cfValues) {
                const f = extractField(cf);
                if (f) fields.push(f);
              }
            }

            // Format 6: CUSTOMFIELD (singular) as string field name or object
            const customField = svc.CUSTOMFIELD || svc.CustomField || svc.customfield;
            if (customField && fields.length === 0) {
              if (typeof customField === 'string' && customField.trim()) {
                fields.push({ name: customField.trim(), key: customField.trim(), type: 'TEXT', required: true });
              } else if (typeof customField === 'object') {
                const f = extractField(customField);
                if (f) fields.push(f);
              }
            }

            // Format 7: Direct CUSTOMNAME on the service object (simple single-field)
            const directCustomName = svc.CUSTOMNAME || svc.customname || svc.CustomName;
            if (directCustomName && typeof directCustomName === 'string' && directCustomName.trim() && fields.length === 0) {
              fields.push({ name: directCustomName.trim(), key: directCustomName.trim(), type: 'TEXT', required: true });
            }

            // Format 8: FIELDS as array or object
            const fieldsObj = svc.FIELDS || svc.Fields || svc.fields;
            if (fieldsObj && fields.length === 0) {
              if (Array.isArray(fieldsObj)) {
                for (const cf of fieldsObj) {
                  if (typeof cf === 'string') {
                    fields.push({ name: cf.trim(), key: cf.trim(), type: 'TEXT', required: true });
                  } else {
                    const f = extractField(cf);
                    if (f) fields.push(f);
                  }
                }
              } else if (typeof fieldsObj === 'object') {
                const fValues = Object.values(fieldsObj) as any[];
                for (const cf of fValues) {
                  if (typeof cf === 'string') {
                    fields.push({ name: cf.trim(), key: cf.trim(), type: 'TEXT', required: true });
                  } else {
                    const f = extractField(cf);
                    if (f) fields.push(f);
                  }
                }
              }
            }

            // Diagnostic: log SERVER products that end up with no custom fields
            const svcType = (svc.SERVICETYPE || 'IMEI').toUpperCase();
            if (svcType === 'SERVER' && fields.length === 0) {
              const svcKeys = Object.keys(svc).filter(k => 
                k.toUpperCase().includes('CUSTOM') || k.toUpperCase().includes('FIELD') 
                || k.toUpperCase().includes('REQUIRE') || k.toUpperCase().includes('INPUT')
              );
              if (svcKeys.length > 0) {
                console.log(`[SD-Unlocker] SERVER service "${svc.SERVICENAME}" (${svc.SERVICEID}) has unhandled field keys: ${svcKeys.join(', ')}`);
                svcKeys.forEach(k => console.log(`  ${k}: ${JSON.stringify(svc[k]).substring(0, 200)}`));
              }
            }

            services.push({
              serviceId: String(svc.SERVICEID || ''),
              serviceName: svc.SERVICENAME || '',
              credit: parseFloat(svc.CREDIT || '0'),
              serviceType: svcType,
              supportsQnt: svc.QNT === '1' || svc.QNT === 'YES' || svc.QNT === true,
              minQnt: parseInt(svc.MINQNT || '0', 10),
              maxQnt: parseInt(svc.MAXQNT || '0', 10),
              deliveryTime: svc.TIME || '',
              groupName,
              fields: (() => {
                const allFields: ServiceField[] = [];
                // For IMEI-type services, add IMEI field only if no custom field already contains "IMEI"
                if (svcType === 'IMEI') {
                  const hasImeiField = fields.some(f =>
                    f.name.toUpperCase().includes('IMEI') || f.key.toUpperCase().includes('IMEI')
                  );
                  if (!hasImeiField) {
                    allFields.push({
                      name: 'IMEI',
                      key: 'IMEI',
                      type: 'NUMBER',
                      required: true,
                    });
                  }
                }

                // Only use actual custom fields from the API response
                // Do NOT fabricate default fields for SERVER products
                // If the API doesn't define custom fields, the product doesn't need any input

                allFields.push(...fields);
                return allFields;
              })(),
            });
          }
        }
      }

      console.log(`[SD-Unlocker] Fetched ${services.length} services`);
      return services;
    } catch (error: any) {
      console.error('[SD-Unlocker] fetchServiceList failed:', error.message);
      throw error;
    }
  }

  // ============================================
  // 2. Place Order (placeimeiorder)
  // ============================================
  async placeOrder(
    serviceId: string,
    imei: string,
    quantity?: number,
    customFields?: Record<string, string>
  ): Promise<PlaceOrderResult> {
    try {
      // Build XML parameters
      let xml = `<PARAMETERS><ID>${serviceId}</ID><IMEI>${imei}</IMEI>`;
      
      if (quantity && quantity > 1) {
        xml += `<QNT>${quantity}</QNT>`;
      }

      if (customFields && Object.keys(customFields).length > 0) {
        const base64Fields = Buffer.from(JSON.stringify(customFields)).toString('base64');
        xml += `<CUSTOMFIELD>${base64Fields}</CUSTOMFIELD>`;
      }

      xml += '</PARAMETERS>';

      console.log(`[SD-Unlocker] Placing order: serviceId=${serviceId}, imei=${imei}, qnt=${quantity || 1}`);
      const data = await this.apiRequest('placeimeiorder', xml);

      if (data.ERROR) {
        const errMsg = Array.isArray(data.ERROR) ? data.ERROR[0]?.MESSAGE : String(data.ERROR);
        console.error('[SD-Unlocker] placeOrder error:', errMsg);
        return {
          success: false,
          referenceId: '',
          message: errMsg,
          rawResponse: data,
        };
      }

      if (data.SUCCESS) {
        const successArr = Array.isArray(data.SUCCESS) ? data.SUCCESS : [data.SUCCESS];
        const refId = successArr[0]?.REFERENCEID || successArr[0]?.ReferenceId || '';
        console.log(`[SD-Unlocker] Order placed successfully. ReferenceId: ${refId}`);
        return {
          success: true,
          referenceId: String(refId),
          rawResponse: data,
        };
      }

      return {
        success: false,
        referenceId: '',
        message: 'Unknown response format',
        rawResponse: data,
      };
    } catch (error: any) {
      console.error('[SD-Unlocker] placeOrder failed:', error.message);
      return {
        success: false,
        referenceId: '',
        message: error.message,
      };
    }
  }

  // ============================================
  // 3. Get Order Status (getimeiorder)
  // ============================================
  async getOrderStatus(referenceId: string): Promise<OrderStatusResult> {
    try {
      const xml = `<PARAMETERS><ID>${referenceId}</ID></PARAMETERS>`;
      const data = await this.apiRequest('getimeiorder', xml);

      if (data.ERROR) {
        const errMsg = Array.isArray(data.ERROR) ? data.ERROR[0]?.MESSAGE : String(data.ERROR);
        return {
          success: false,
          status: 'UNKNOWN',
          message: errMsg,
          rawResponse: data,
        };
      }

      if (data.SUCCESS) {
        const successArr = Array.isArray(data.SUCCESS) ? data.SUCCESS : [data.SUCCESS];
        // DHRU nests the actual data in SUCCESS.RESULT or directly in SUCCESS
        const rawResult = successArr[0] || {};
        const result = rawResult.RESULT || rawResult.Result || rawResult;
        
        // Get STATUS — DHRU returns numeric codes as strings ('0'-'5')
        const rawStatus = String(result.STATUS || result.Status || result.status || '').trim();
        
        console.log(`[SD-Unlocker] getOrderStatus ref=${referenceId}: rawStatus="${rawStatus}", keys=${Object.keys(result).join(',')}`);

        // DHRU Fusion numeric status mapping
        const numericStatusMap: Record<string, string> = {
          '0': 'WAITING',      // waiting / in queue — في الطابور
          '1': 'PROCESSING',   // processing — بدأ المعالجة
          '2': 'REJECTED',     // rejected
          '3': 'REJECTED',     // failed
          '4': 'COMPLETED',    // completed successfully
          '5': 'REJECTED',     // cancelled
        };

        // String-based status mapping (fallback for other providers)
        const stringStatusMap: Record<string, string> = {
          'COMPLETED': 'COMPLETED',
          'SUCCESS': 'COMPLETED',
          'DONE': 'COMPLETED',
          'REJECTED': 'REJECTED',
          'CANCELLED': 'REJECTED',
          'FAILED': 'REJECTED',
          'PENDING': 'WAITING',
          'WAITING': 'WAITING',
          'INPROGRESS': 'PROCESSING',
          'IN PROGRESS': 'PROCESSING',
          'PROCESSING': 'PROCESSING',
        };

        // Try numeric mapping first, then string mapping
        let mappedStatus = numericStatusMap[rawStatus]
          || stringStatusMap[rawStatus.toUpperCase()]
          || 'PROCESSING'; // default

        // Extract codes/keys from response — check multiple possible field names
        const codes = result.CODE || result.CODES || result.Code || result.Codes
          || result.RESULT || result.Result || result.result
          || result.UNLOCKCODE || result.UnlockCode
          || result.KEY || result.Key || '';

        console.log(`[SD-Unlocker] Order ref=${referenceId}: mapped=${mappedStatus}, codes=${codes ? String(codes).substring(0, 50) : 'none'}`);

        return {
          success: true,
          status: mappedStatus,
          codes: codes ? String(codes) : undefined,
          rawResponse: data,
        };
      }

      return {
        success: false,
        status: 'UNKNOWN',
        message: 'Unknown response format',
        rawResponse: data,
      };
    } catch (error: any) {
      console.error('[SD-Unlocker] getOrderStatus failed:', error.message);
      return {
        success: false,
        status: 'UNKNOWN',
        message: error.message,
      };
    }
  }

  // ============================================
  // 4. Get Balance (getbalance)
  // ============================================
  async getBalance(): Promise<BalanceResult> {
    try {
      const data = await this.apiRequest('getbalance');

      if (data.ERROR) {
        const errMsg = Array.isArray(data.ERROR) ? data.ERROR[0]?.MESSAGE : String(data.ERROR);
        return { success: false, balance: 0, message: errMsg };
      }

      if (data.SUCCESS) {
        const successArr = Array.isArray(data.SUCCESS) ? data.SUCCESS : [data.SUCCESS];
        const balanceVal = successArr[0]?.BALANCE || successArr[0]?.Balance || successArr[0]?.balance || 0;
        return {
          success: true,
          balance: parseFloat(String(balanceVal)),
        };
      }

      return { success: false, balance: 0, message: 'Unknown response format' };
    } catch (error: any) {
      console.error('[SD-Unlocker] getBalance failed:', error.message);
      return { success: false, balance: 0, message: error.message };
    }
  }
}

export const externalProvider = new ExternalProviderService();
