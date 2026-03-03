import prisma from '../config/database';
import { externalProvider, SDService } from './externalProvider';

/**
 * Product Sync Service
 * Fetches services from DHRU FUSION and syncs them to the local database.
 * - Creates categories from group names
 * - Maps DHRU FUSION fields to Product model
 * - Updates existing products by externalId, creates new ones
 * - Does NOT delete local products missing from provider (admin may have custom products)
 */
export class SyncService {
  
  /**
   * Sync all products from DHRU FUSION
   * Returns summary of created/updated/skipped
   */
  async syncProducts(markupPercent: number): Promise<{
    created: number;
    updated: number;
    skipped: number;
    total: number;
    errors: string[];
  }> {
    const errors: string[] = [];
    let created = 0;
    let updated = 0;
    let skipped = 0;

    try {
      // Fetch services from DHRU FUSION
      console.log('[Sync] Fetching services from DHRU FUSION...');
      const services = await externalProvider.fetchServiceList();
      console.log(`[Sync] Received ${services.length} services`);

      // Group by groupName → create/find categories
      const groupNames = [...new Set(services.map(s => s.groupName))];
      const categoryMap = new Map<string, string>(); // groupName → categoryId

      for (const gName of groupNames) {
        try {
          // Find or create category
          let category = await prisma.category.findFirst({
            where: { name: gName },
          });

          if (!category) {
            category = await prisma.category.create({
              data: {
                name: gName,
                nameAr: gName, // Admin can translate later
                isActive: true,
                sortOrder: 0,
              },
            });
            console.log(`[Sync] Created category: ${gName}`);
          }

          categoryMap.set(gName, category.id);
        } catch (err: any) {
          errors.push(`Category "${gName}": ${err.message}`);
        }
      }

      // Upsert each service as a product
      for (const svc of services) {
        try {
          const categoryId = categoryMap.get(svc.groupName);
          if (!categoryId) {
            errors.push(`No category for service ${svc.serviceId} (${svc.serviceName})`);
            skipped++;
            continue;
          }

          // Calculate selling price with markup
          const sellingPrice = Math.round(svc.credit * (1 + markupPercent / 100) * 100) / 100;

          const productData = {
            name: svc.serviceName,
            price: sellingPrice,
            costPrice: svc.credit,
            type: 'SERVICE' as const,
            serviceType: svc.serviceType,
            categoryId,
            externalId: svc.serviceId,
            fields: svc.fields.length > 0 ? JSON.stringify(svc.fields) : null,
            deliveryTime: svc.deliveryTime || null,
            supportsQnt: svc.supportsQnt,
            minQnt: svc.minQnt,
            maxQnt: svc.maxQnt,
            groupName: svc.groupName,
            isActive: true,
          };

          // Check if product exists by externalId
          const existing = await prisma.product.findFirst({
            where: { externalId: svc.serviceId },
          });

          if (existing) {
            // Update existing — preserve admin-set price if they changed it, update cost
            await prisma.product.update({
              where: { id: existing.id },
              data: {
                name: svc.serviceName,
                costPrice: svc.credit,
                serviceType: svc.serviceType,
                fields: productData.fields,
                deliveryTime: productData.deliveryTime,
                supportsQnt: svc.supportsQnt,
                minQnt: svc.minQnt,
                maxQnt: svc.maxQnt,
                groupName: svc.groupName,
                categoryId,
              },
            });
            updated++;
          } else {
            // Create new product
            await prisma.product.create({ data: productData });
            created++;
          }
        } catch (err: any) {
          errors.push(`Service ${svc.serviceId} (${svc.serviceName}): ${err.message}`);
          skipped++;
        }
      }

      console.log(`[Sync] Done. Created: ${created}, Updated: ${updated}, Skipped: ${skipped}, Errors: ${errors.length}`);

      return {
        created,
        updated,
        skipped,
        total: services.length,
        errors,
      };
    } catch (error: any) {
      console.error('[Sync] syncProducts failed:', error.message);
      throw error;
    }
  }

  /**
   * Get provider balance
   */
  async getProviderBalance() {
    return externalProvider.getBalance();
  }
}

export const syncService = new SyncService();
