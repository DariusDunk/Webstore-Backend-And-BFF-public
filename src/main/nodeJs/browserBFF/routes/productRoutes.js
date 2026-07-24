import express from 'express';
import {Backend_Url, WEB_CLIENT_NAME} from './config.js';
import fetchWithSessionTokens from "../services/requestTokenManager.js";
import axiosBackendClient from '../axiosBackendClient.js';
import {getTopProductsOfTopSales, getTopProductsOfTopCategories} from "../services/homePageRequests.js";
import multer from "multer";
import FormData from "form-data";
const upload = multer();

const router = express.Router();

const timestamp = () => {
    const now = new Date();
    return `[${now.getFullYear()}-${String(now.getMonth()+1).padStart(2,'0')}-${String(now.getDate()).padStart(2,'0')} ${String(now.getHours()).padStart(2,'0')}-${String(now.getMinutes()).padStart(2,'0')}-${String(now.getSeconds()).padStart(2,'0')}]`;
};

router.post("/image-search",
    upload.single("image"),
    async (req, res) => {

        const sessionId = req.cookies.session_id;

        if (!req.file) {
            return res.status(400).json({ error: "Missing file" });
        }

        const file = req.file;
        const formData = new FormData();

        formData.append(
            "image",
            file.buffer,
            file.originalname
        );

        try
        {
            const response = await fetchWithSessionTokens(sessionId, async (
                ) => {
                    return await axiosBackendClient.post(
                        `${Backend_Url}/product/imageSearch`,
                        formData,
                        {
                            headers: {
                                'x-client_type': WEB_CLIENT_NAME,
                                // ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                                // ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                                ...(formData.getHeaders())
                            },
                            bffContext: {
                                req, res
                            }
                        }
                    );
                },
                {req, res});

          return  res.status(response.status).json(response.data);
        }
        catch (error) {
            if (error.response) {
                console.warn(`${timestamp()} Handled backend error for image search`);
                return res.status(error.response.status || 500).json(error.response.data);
            }

            console.error('-------------------Unexpected error for image search-------------------\n', error);
            return res.status(error.response?.status || 500).json({error: 'Internal server error'});
        }

    }
);

router.post("/catman", async (req, res) => {
    const requestBody = req.body;
    const sessionId = req.cookies.session_id;
    const{categories, manufacturers, page} = requestBody;

    try
    {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                return await axiosBackendClient.post(`${Backend_Url}/product/catManSearch`,
                {categories:categories, manufacturers: manufacturers, page: page}, {
                        headers: {
                            'Content-Type': 'application/json',
                            'x-client_type': WEB_CLIENT_NAME,
                            ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                            ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                        },
                        bffContext: {
                            req, res
                        }
                    }
                );
            },
            {req, res});

        const responseData = response.data;

        console.log("catman response: ", responseData);

        res.status(response.status).json(responseData);

    }catch (error) {
        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for catman search`);
            return res.status(error.response.status || 500).json(error.response.data);
        }

        console.error('-------------------Unexpected error for catman search-------------------\n', error);
        return res.status(error.response?.status || 500).json({error: 'Internal server error'});
    }
});


router.post('/byCodesWithStockValidation', async (req, res) => {
        const productCodesQuantityPairs = req.body;
        const sessionId = req.cookies.session_id;

        // console.log('Product codes to fetch:', productCodesQuantityPairs);

        try {
            const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                    return await axiosBackendClient.post(`${Backend_Url}/product/codes/stockValidation`, productCodesQuantityPairs, {
                        headers: {
                            'Content-Type': 'application/json',
                            'x-client_type': WEB_CLIENT_NAME,
                            ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                            ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                        },
                        bffContext: {
                            req, res
                        }
                    });
                },
                {req, res});

            const responseData = response.data;

            // console.log('Products responseData:', responseData);

            return res.status(response.status).json(responseData);
        } catch (error) {

            if (error.response) {
                console.warn(`${timestamp()} Handled backend error for fetching product data`);
                return res.status(error.response.status || 500).json(error.response.data);
            }

            console.error('-------------------Unexpected error fetching product data-------------------\n', error);
            return res.status(error.response?.status || 500).json({error: 'Internal server error'});

        }

    }
);

router.get('/homePage', async (req, res) => {

    const sessionId = req.cookies.session_id;

    try {

        const productRowsResponse = await fetchWithSessionTokens(
            sessionId,
            async (sessionData) => {
                const results = await Promise.all([
                    getTopProductsOfTopSales(req, res, sessionData),
                    getTopProductsOfTopCategories(req, res, sessionData)
                ]);

                // console.log("RAW RESULTS:", JSON.stringify(results, null, 2));

                const flattenedList = results
                    .map(r => Array.isArray(r) ? r : [])
                    .flat();

                return {
                    status: 200,
                    data: flattenedList,
                    headers: results[0]?.headers || results[1]?.headers || {}}
            },
            { req, res }
        );

        // console.log("Home page response: ", JSON.stringify(productRowsResponse));

        return res.status(200).json(productRowsResponse.data);

    } catch (error) {
        console.error('Search: Error fetching home page data:', error);
        return res.status(error.response?.status || 500).end();
    }
});

router.get('/manufacturer/:manufacturerName/p:page', async (req, res) => {
    const {manufacturerName, page} = req.params;
    const sort = req.query.sort;
    const sessionId = req.cookies.session_id;

    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.get(`${Backend_Url}/product/manufacturer/${manufacturerName}/p${page}?${new URLSearchParams({sort: sort || ''})}`, {
                headers: {
                    'Content-Type': 'application/json',
                    'x-client_type': WEB_CLIENT_NAME,
                    ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                    ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                },
                bffContext: {
                    req, res
                }
            });
        }, {req, res});

        const responseData = await response.data;
        return res.status(response.status).json(responseData);

    } catch (error) {
        console.error('Manufacturer: Error fetching data:', error);
        return res.status(error.response?.status || 500).end();
    }
});

router.get('/category/:categoryName/p:page', async (req, res) => {
    const {categoryName, page} = req.params;
    const sort = req.query.sort;
    const sessionId = req.cookies.session_id;

    try {

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.get(`${Backend_Url}/product/category/${categoryName}/p${page}?${new URLSearchParams({sort: sort || ''})}`, {
                headers: {
                    'Content-Type': 'application/json',
                    'x-client_type': WEB_CLIENT_NAME,
                    ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                    ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                },
                bffContext: {
                    req, res
                }
            });
        }, {req, res});

        const responseData = await response.data;
        return res.status(response.status).json(responseData);

    } catch (error) {
        console.error('Category: Error fetching data:', error);
        return res.status(error.response?.status || 500).end();
    }
});

router.get(`/review/overview/:productCode`, async (req, res) => {
    const {productCode} = req.params;
    const sessionId = req.cookies.session_id;

    if (!productCode) {
        return res.status(400).end();
    }

    try {

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.get(`${Backend_Url}/product/${productCode}/review/overview`, {
                headers: {
                    'Content-Type': 'application/json',
                    'x-client_type': WEB_CLIENT_NAME,
                    ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                    ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                },
                bffContext: {
                    req, res
                }
            });
        }, {req, res});

        const responseData = await response.data;
        return res.status(response.status).json(responseData);

    } catch (error) {
        console.error('Error fetching product review overview from backend:', error);
        return res.status(error.response?.status || 500).end();
    }
})

router.get('/detail/:productCode', async (req, res) => {
    const {productCode} = req.params;
    const sessionId = req.cookies.session_id;

    if (!productCode) {
        return res.status(400).end();
    }
    try {
        const response =
         await fetchWithSessionTokens(sessionId, async (sessionData) => {

                 const [prodRes, reviewRes] = await Promise.all([
                     axiosBackendClient.get(`${Backend_Url}/product/detail/${productCode}`, {
                         headers: {
                             'Content-Type': 'application/json',
                             'x-client_type': WEB_CLIENT_NAME,
                             ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                             ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                         },
                         bffContext: {
                             req, res
                         }
                     }),
                     axiosBackendClient.get(`${Backend_Url}/product/${productCode}/review/overview`, {
                         headers: {
                             'Content-Type': 'application/json',
                             ...(sessionId && {'x-session-id': sessionId}),
                         },
                         bffContext: {
                             req, res
                         }
                     })
                 ])

                 return {
                     status: 200,
                     data: {
                         productDetails: prodRes.data,
                         ratingOverview: reviewRes.data
                     },
                     headers: prodRes.headers,
                 };
             }
            , {req, res});

        // const productDetails = productDetailResponse.data;
        // const ratingOverview = ratingOverviewResponse.data;

        // console.log('productDetails response:', JSON.stringify(response));

        return res.status(200).json(response.data);

    } catch (error) {
        console.error('Error fetching detailed product data from backend:', error);
        return res.status(error.response?.status || 500).end();
    }
});

router.get('/suggest/:name', async (req, res) => {
    try {

        const name = req.params.name;
        const sessionId = req.cookies.session_id;

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.get(`${Backend_Url}/product/suggest?${new URLSearchParams({name: name || ''})}`, {
                headers: {
                    'Content-Type': 'application/json',
                    'x-client_type': WEB_CLIENT_NAME,
                    ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                    ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                },
                bffContext: {
                    req, res
                }
            });
        }, {req, res});

        const data = await response.data;

        return res.status(response.status).json(data);

    } catch (error) {
        console.error('Suggest: Error fetching suggestions from backend:', error);
        return res.status(error.response?.status || 500).end();
    }
});

router.get(`/search`, async (req, res) => {

    const {searchText, page, sort} = req.query;
    const sessionId = req.cookies.session_id;

    try {

        const url = new URL('/product/search', Backend_Url);

        url.searchParams.set('name', searchText);
        url.searchParams.set('page', page);
        url.searchParams.set('sort', sort || '');

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.get(url.toString(), {
                headers: {
                    'Content-Type': 'application/json',
                    'x-client_type': WEB_CLIENT_NAME,
                    ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                    ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                },
                bffContext: {
                    req, res
                }
            });
        }, {req, res});

        const data = await response.data;
        return res.status(response.status).json(data);

    } catch (error) {
        console.error('Search: Error fetching data:', error);
        return res.status(error.response?.status || 500).end();
    }
});

router.get('/category-filter/:category/pg:page', async (req, res) => {

    // console.log('filter search');

    const page = parseInt(req.params.page, 10);
    if (isNaN(page)) {
        return res.status(400).json({error: 'Invalid page parameter'});
    }

    console.log("Query: \n" + JSON.stringify(req.query));

    const category = decodeURIComponent(req.params.category);
    const {filters = {}, sort} = req.query;
    const sessionId = req.cookies.session_id;
    let minPrice = 0;
    let maxPrice = Infinity;

    if (filters.pr) {

        const priceRange = filters.pr.split('-');
        minPrice = parseInt(priceRange[0], 10) || 0;
        maxPrice = parseInt(priceRange[1], 10) || Infinity;
    }

    let manufacturers = [];

    if (filters.m) {
        if (Array.isArray(filters.m)) {
            manufacturers = filters.m.map(decodeURIComponent);
        } else if (filters.m.includes(',')) {
            manufacturers = filters.m.split(',').map(decodeURIComponent);
        } else {
            manufacturers = [decodeURIComponent(filters.m)];
        }
    }

    const rating = filters.r ? filters.r : null;

    // console.log("ratings: ", rating)

    const attributes = {};
    Object.keys(filters).forEach(key => {
        if (key.startsWith('a')) {
            const nameId = key.slice(1);  //   '1' for 'a1'
            attributes[nameId] = filters[key].split(',').map(decodeURIComponent);
        }
    });

    const requestBody = {
        filter_attributes: attributes,
        product_category: category,
        price_lowest: minPrice,
        price_highest: maxPrice,
        manufacturer_names: manufacturers,
        rating: rating,
        sort: sort
    };

    try {

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.post(`${Backend_Url}/product/filter/${page}`, requestBody, {
                headers: {
                    'Content-Type': 'application/json',
                    'x-client_type': WEB_CLIENT_NAME,
                    ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                    ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                },
                bffContext: {
                    req, res
                }
            });
        }, {req, res});

        const responseData = await response.data;
        return res.status(response.status).json(responseData);

    } catch (error) {
        console.error('Error fetching products with filters:', error);
        return res.status(error.response?.status || 500).end();
    }
});

router.post('/getPagedReviews', async (req, res) => {

    const productCode = req.body.productCode;
    const page = req.body.page;
    const sort = req.body.sortOrder;
    const verifiedOnly = req.body.verifiedOnly;
    const ratingValue = req.body.ratingValue
    const sessionId = req.cookies.session_id;

    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {

            // console.log("Session data for paged review: ", sessionData);

            // console.log('pagedReview response:', respons);

            return await axiosBackendClient.post(`${Backend_Url}/product/reviews/paged`, {
                product_code: productCode,
                page,
                sort_order: sort,
                verified_only: verifiedOnly,
                rating_value: ratingValue
            }, {
                headers: {
                    'Content-Type': 'application/json',
                    'x-client_type': WEB_CLIENT_NAME,
                    ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                    ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                },
                bffContext: {
                    req, res
                }
            });
        }, {req, res})

        // console.log('Paged reviews response body:', response);
        // console.log('response?.status:', response?.status);
        // console.log('response?.data:', response?.data);

        const responseData = response?.data;

        // console.log('paged reviews response status:', response?.status);
        // console.log('paged reviews response data:', responseData);

        return res.status(response.status).json(responseData);

    } catch (error) {
        console.error('Reviews: Error fetching paged reviews data: ', error);
        return res.status(error.response?.status || 500).json({error: 'Internal server error'});
    }
})

router.get(`/getReview/:productCode`, async (req, res) => {
    const {productCode} = req.params;
    const sessionId = req.cookies.session_id;

    try {

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.get(`${Backend_Url}/product/review/specific?${new URLSearchParams({productCode: productCode || ''})}`, {
                headers: {
                    'Content-Type': 'application/json',
                    'x-client_type': WEB_CLIENT_NAME,
                   ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                    ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                },
                bffContext: {
                    req, res
                }
            })
        }, {req, res})

        const responseData = await response.data;
        return res.status(response.status).json(responseData);

    } catch (error) {

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for fetching specific review data`);
            return res.status(error.response.status||500).json(error.response.data);
        }

        console.error('-------------------Unexpected error fetching specific review data-------------------\n', error);
        return res.status(error.response?.status || 500).json({error: 'Internal server error'});
    }
})

router.post(`/addReview`, async (req, res) => {
    const productCode = req.body.productCode;
    const rating = req.body.rating;
    const reviewText = req.body.reviewText;
    const sessionId = req.cookies.session_id;

    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.post(`${Backend_Url}/product/review/add`, {
                product_code: productCode,
                rating: rating,
                review_text: reviewText
            }, {
                headers: {
                    'Content-Type': 'application/json',
                    'x-client_type': WEB_CLIENT_NAME,
                   ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                    ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                },
                bffContext: {
                    req, res
                }
            })
        }, {req, res})

        const responseData = await response.data;
        return res.status(response.status).json(responseData);
    } catch (error) {

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for creating review`);
            return res.status(error.response.status||500).json(error.response.data);
        }

        console.error('-------------------Unexpected error creating review-------------------\n', error);
        return res.status(error.response?.status || 500).json({error: 'Internal server error'});
    }
})

router.post(`/updateReview`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    const productCode = req.body.productCode;
    const rating = req.body.rating;
    const reviewText = req.body.reviewText;

    try {

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.patch(`${Backend_Url}/product/review/update`, {
                product_code: productCode,
                rating: rating,
                review_text: reviewText
            }, {
                headers: {
                    'Content-Type': 'application/json',
                    'x-client_type': WEB_CLIENT_NAME,
                   ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                    ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                },
                bffContext: {
                    req, res
                }
            })
        }, {req, res})

        const responseData = await response.data;
        return res.status(response.status).json(responseData);

    } catch (error) {

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for updating the review`);
            return res.status(error.response.status||500).json(error.response.data);
        }

        console.error('-------------------Error updating the review-------------------\n', error);
        return res.status(error.response?.status || 500).end();
    }
})

router.post(`/deleteReview`, async (req, res) => {
    const {productCode} = req.body;
    const sessionId = req.cookies.session_id;

    try {

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.delete(`${Backend_Url}/product/review/delete?${new URLSearchParams({product_code: productCode || ''})}`, {
                headers: {
                    'Content-Type': 'application/json',
                    'x-client_type': WEB_CLIENT_NAME,
                   ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                    ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                },
                bffContext: {
                    req, res
                }
            })
        }, {req, res})

        const responseData = await response.data;
        return res.status(response.status).json(responseData);

    } catch (error) {

        console.error('Error deleting the review ', error);
        return res.status(error.response?.status || 500).end();
    }
})

export default router;
