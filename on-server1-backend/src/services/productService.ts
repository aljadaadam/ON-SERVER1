import prisma from '../config/database';

/** Parse fields JSON string into array for API response */
function parseProductFields(product: any) {
  if (!product) return product;
  if (product.fields && typeof product.fields === 'string') {
    try { product.fields = JSON.parse(product.fields); } catch { product.fields = null; }
  }
  return product;
}

function parseProductsFields(products: any[]) {
  return products.map(p => parseProductFields(p));
}

export class ProductService {
  async getAll(params: {
    page?: number;
    limit?: number;
    categoryId?: string;
    type?: string;
    serviceType?: string;
    groupName?: string;
    search?: string;
    featured?: boolean;
  }) {
    const page = params.page || 1;
    const limit = params.limit || 20;
    const skip = (page - 1) * limit;

    const where: any = { isActive: true };
    if (params.categoryId) where.categoryId = params.categoryId;
    if (params.type) where.type = params.type;
    if (params.serviceType) where.serviceType = params.serviceType;
    if (params.groupName) where.groupName = { contains: params.groupName };
    if (params.featured) where.isFeatured = true;
    if (params.search) {
      where.OR = [
        { name: { contains: params.search } },
        { nameAr: { contains: params.search } },
        { description: { contains: params.search } },
        { groupName: { contains: params.search } },
      ];
    }

    const [products, total] = await Promise.all([
      prisma.product.findMany({
        where,
        include: { category: { select: { id: true, name: true, nameAr: true, icon: true } } },
        orderBy: [{ sortOrder: 'asc' }, { createdAt: 'desc' }],
        skip,
        take: limit,
      }),
      prisma.product.count({ where }),
    ]);

    return {
      products: parseProductsFields(products),
      pagination: {
        page,
        limit,
        total,
        totalPages: Math.ceil(total / limit),
      },
    };
  }

  async getById(id: string) {
    const product = await prisma.product.findUnique({
      where: { id },
      include: { category: true },
    });

    if (!product) {
      throw Object.assign(new Error('Product not found'), { statusCode: 404 });
    }
    return parseProductFields(product);
  }

  async getFeatured() {
    const products = await prisma.product.findMany({
      where: { isFeatured: true, isActive: true },
      include: { category: { select: { id: true, name: true, icon: true } } },
      orderBy: { sortOrder: 'asc' },
      take: 10,
    });
    return parseProductsFields(products);
  }

  async getCategories() {
    return prisma.category.findMany({
      where: { isActive: true, parentId: null },
      include: {
        children: { where: { isActive: true }, orderBy: { sortOrder: 'asc' } },
        _count: { select: { products: true } },
      },
      orderBy: { sortOrder: 'asc' },
    });
  }

  async getGroups(serviceType?: string) {
    const where: any = { isActive: true, groupName: { not: null } };
    if (serviceType) where.serviceType = serviceType;
    const products = await prisma.product.findMany({
      where,
      select: { groupName: true },
      distinct: ['groupName'],
      orderBy: { groupName: 'asc' },
    });
    return products.map(p => p.groupName).filter(Boolean);
  }

  // Admin methods
  async create(data: any) {
    return prisma.product.create({ data });
  }

  async update(id: string, data: any) {
    return prisma.product.update({ where: { id }, data });
  }

  async delete(id: string) {
    return prisma.product.update({ where: { id }, data: { isActive: false } });
  }

  async createCategory(data: any) {
    return prisma.category.create({ data });
  }

  async updateCategory(id: string, data: any) {
    return prisma.category.update({ where: { id }, data });
  }

  async deleteCategory(id: string) {
    // Delete children first, then the category
    await prisma.category.deleteMany({ where: { parentId: id } });
    return prisma.category.delete({ where: { id } });
  }
}

export const productService = new ProductService();
