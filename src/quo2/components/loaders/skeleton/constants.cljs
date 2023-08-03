(ns quo2.components.loaders.skeleton.constants)

(def ^:const layout-dimensions
  {:messages      {:height      56
                   :padding-top 12}
   :list-items    {:height      56
                   :padding-top 12}
   :notifications {:height      90
                   :padding-top 8}})

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
   :list-items    {0 {:author  {:width         112
                                :height        8
                                :margin-bottom 0}
                      :message {:width         144
                                :height        16
                                :margin-bottom 8}}
                   1 {:author  {:width         96
                                :height        8
                                :margin-bottom 0}
                      :message {:width         212
                                :height        16
                                :margin-bottom 8}}
                   2 {:author  {:width         80
                                :height        8
                                :margin-bottom 0}
                      :message {:width         249
                                :height        16
                                :margin-bottom 8}}
                   3 {:author  {:width         124
                                :height        8
                                :margin-bottom 0}
                      :message {:width         156
                                :height        16
                                :margin-bottom 8}}}
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
                                 :margin-bottom 0}}}})
