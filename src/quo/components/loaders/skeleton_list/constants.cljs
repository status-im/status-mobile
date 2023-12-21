(ns quo.components.loaders.skeleton-list.constants)

(def ^:const layout-dimensions
  {:messages      {:height      56
                   :padding-top 12}
   :list-items    {:height      56
                   :padding-top 12}
   :notifications {:height      90
                   :padding-top 8}
   :assets        {:height      56
                   :padding-top 12}})

(def ^:const content-dimensions
  {:messages      {0 {:author  {:width         112
                                :height        8
                                :margin-bottom 8}
                      :message {:width         144
                                :height        16
                                :margin-bottom 0}}
                   1 {:author  {:width         96
                                :height        8
                                :margin-bottom 8}
                      :message {:width         212
                                :height        16
                                :margin-bottom 0}}
                   2 {:author  {:width         80
                                :height        8
                                :margin-bottom 8}
                      :message {:width         249
                                :height        16
                                :margin-bottom 0}}
                   3 {:author  {:width         124
                                :height        8
                                :margin-bottom 8}
                      :message {:width         156
                                :height        16
                                :margin-bottom 0}}}
   :list-items    {0 {:author  {:width         144
                                :height        16
                                :margin-bottom 8}
                      :message {:width         112
                                :height        8
                                :margin-bottom 0}}
                   1 {:author  {:width         212
                                :height        16
                                :margin-bottom 8}
                      :message {:width         96
                                :height        8
                                :margin-bottom 0}}
                   2 {:author  {:width         249
                                :height        16
                                :margin-bottom 8}
                      :message {:width         80
                                :height        8
                                :margin-bottom 0}}
                   3 {:author  {:width         156
                                :height        16
                                :margin-bottom 8}
                      :message {:width         124
                                :height        8
                                :margin-bottom 0}}}
   :notifications {0 {:author   {:width         109
                                 :height        8
                                 :margin-bottom 8}
                      :message  {:width         167
                                 :height        16
                                 :margin-bottom 8}
                      :message2 {:width         242
                                 :height        30
                                 :margin-bottom 0}}
                   1 {:author   {:width         165
                                 :height        8
                                 :margin-bottom 8}
                      :message  {:width         112
                                 :height        16
                                 :margin-bottom 8}
                      :message2 {:width         294
                                 :height        30
                                 :margin-bottom 0}}
                   2 {:author   {:width         136
                                 :height        8
                                 :margin-bottom 8}
                      :message  {:width         178
                                 :height        16
                                 :margin-bottom 8}
                      :message2 {:width         178
                                 :height        30
                                 :margin-bottom 0}}
                   3 {:author   {:width         136
                                 :height        8
                                 :margin-bottom 8}
                      :message  {:width         166
                                 :height        16
                                 :margin-bottom 8}
                      :message2 {:width         256
                                 :height        30
                                 :margin-bottom 0}}}
   :assets        {0 {:author   {:width         60
                                 :height        14
                                 :margin-bottom 8}
                      :message  {:width         92
                                 :height        10
                                 :margin-bottom 0}
                      :author2  {:width         80
                                 :height        10
                                 :margin-bottom 8}
                      :message2 {:width        52
                                 :height       10
                                 :margin-right 4}
                      :message3 {:width        52
                                 :height       10
                                 :margin-right 4}}
                   1 {:author   {:width         60
                                 :height        14
                                 :margin-bottom 8}
                      :message  {:width         92
                                 :height        10
                                 :margin-bottom 0}
                      :author2  {:width         100
                                 :height        10
                                 :margin-bottom 8}
                      :message2 {:width        32
                                 :height       10
                                 :margin-right 4}
                      :message3 {:width        32
                                 :height       10
                                 :margin-right 4}}
                   2 {:author   {:width         80
                                 :height        14
                                 :margin-bottom 8}
                      :message  {:width         112
                                 :height        10
                                 :margin-bottom 0}
                      :author2  {:width         80
                                 :height        10
                                 :margin-bottom 8}
                      :message2 {:width        52
                                 :height       10
                                 :margin-right 4}
                      :message3 {:width        52
                                 :height       10
                                 :margin-right 4}}
                   3 {:author   {:width         80
                                 :height        14
                                 :margin-bottom 8}
                      :message  {:width         112
                                 :height        10
                                 :margin-bottom 0}
                      :author2  {:width         100
                                 :height        10
                                 :margin-bottom 8}
                      :message2 {:width        32
                                 :height       10
                                 :margin-right 4}
                      :message3 {:width        32
                                 :height       10
                                 :margin-right 4}}}})
