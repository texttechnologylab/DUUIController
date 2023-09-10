import { PrismaClient } from '@prisma/client'

const prisma: Pris = global.prisma || new PrismaClient()

if (process.env.NODE_ENV === 'development') {
	global.prisma = prisma
}

export { prisma }
