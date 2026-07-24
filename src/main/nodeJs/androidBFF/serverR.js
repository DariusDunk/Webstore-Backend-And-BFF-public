// noinspection DuplicatedCode

import express from 'express';
// import cors from 'cors';
import productRoutes from './routes/productRoutes.js';
import categoryRoutes from './routes/categoryRoutes.js';
import customerRoutes from './routes/customerRoutes.js';
import purchaseRoutes from './routes/purchaseRoutes.js';
import attributeRoutes from './routes/attributeRoutes.js';
import authRoutes from './routes/authRoutes.js';
import cartRoutes from "./routes/cartRoutes.js";
// import adminSessionRoutes from "./routes/admin/adminSessionRoutes.js";
// import adminCategoryRoutes from "./routes/admin/adminCategoryRoutes.js";
// import adminAttributeRoutes from "./routes/admin/adminAttributeRoutes.js";
// import adminSaleRoutes from "./routes/admin/adminSaleRoutes.js";
// import adminProductRoutes from "./routes/admin/adminProductRoutes.js";
// import AdminManufacturerRoutes from "./routes/admin/adminManufacturerRoutes.js";
// import AdminProductImageRoutes from "./routes/admin/AdminProductImageRoutes.js";
// import AdminPurchaseController from "./routes/admin/adminPurchaseRoutes.js";

const app = express();
const port = 3001;

app.use(express.json());

app.use('/product', productRoutes)
app.use('/category', categoryRoutes)
app.use('/customer', customerRoutes)
app.use('/purchase', purchaseRoutes)
app.use('/attribute', attributeRoutes)
app.use('/auth', authRoutes)
app.use('/cart', cartRoutes)
// app.use('/admin/session', adminSessionRoutes)
// app.use('/admin/category', adminCategoryRoutes)
// app.use('/admin/attribute', adminAttributeRoutes)
// app.use('/admin/sale', adminSaleRoutes)
// app.use('/admin/product', adminProductRoutes)
// app.use('/admin/manufacturer', AdminManufacturerRoutes)
// app.use('/admin/product-image', AdminProductImageRoutes)
// app.use('/admin/purchase', AdminPurchaseController)

app.listen(port, () => {
    console.log(`Server running at https://bff.domain externally, and http://browser-bff:${port} internally`);
});

