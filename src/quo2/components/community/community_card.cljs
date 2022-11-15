(ns quo2.components.community.community-card
(:require [quo2.components.markdown.text :as text]
          [quo2.foundations.colors :as colors]
          [quo2.components.community.style :as style]
          [quo2.components.icon :as icons]
          [react-native.core :as rn]))


(defn community-title-and-description [title description image]
  [rn/view
   [rn/view
    {:style {
              :padding-left 15
              :flex-direction :row
              :align-items :center
              }}
    [rn/image
     {:source image
      :style {
               :width 16
               :height 16
               }}
     ]
    [rn/view
     {:style {
               :margin-left 5
               }}
     [text/text {
                  :font-size 15
                  :font-weight 600
                  :font-style :normal
                  :weight :semi-bold
                  }
      title
      ]
     ]
    ]

   [rn/view
    {:style {
              :padding-left 15
              }}
    [text/text {
                 :font-size 13
                 :font-style :normal
                 :font-weight 400
                 :letter-spacing -0.003
                 :weight :regular
                 }
     description
     ]
    ]
   ])

(defn community-stats [member_count likes_count dislike_count]
  [rn/view
   {:style {
             :flex-direction :row
             :align-self :flex-start
             :margin-vertical 10
             :width 271
             :padding-left 15
             }}

   [rn/view
    {:style {
              :flex-direction :row
              :align-items :center
              }}

    [rn/view
     [icons/icon  :i/add-user
      {
        :size 20
        :color colors/neutral-50
        }
      ]
     ]

    [text/text {
                 :font-size 13
                 :font-weight 400
                 :font-style :normal
                 }
     member_count
     ]
    ]

   [rn/view
    {:style {
              :flex-direction :row
              :align-items :center
              :padding-horizontal 10

              }}

    [rn/view
     [icons/icon  :i/add-user
      {
        :size 20
        :color colors/neutral-50
        :left 10
        }
      ]
     ]

    [text/text {
                 :font-size 13
                 :font-weight 400
                 :font-style :normal
                 }
     likes_count
     ]
    ]

   [rn/view
    {:style {
              :flex-direction :row
              :align-items :center
              }}
    [rn/view
     [icons/icon :i/placeholder
      {
        :size 20
        :color colors/neutral-50
        }
      ]
     ]
    [text/text {
                 :font-size 13
                 :font-weight 400
                 :font-style :normal
                 }
     dislike_count
     ]
    ]
   ])


(defn community-card [{:keys [title description image member_count likes_count dislike_count]}]
  [rn/view (merge (style/community-card 16)
                  {:background-color  (colors/theme-colors
                                       colors/white
                                       colors/neutral-90)}
                  {
                    :height 176
                    :width  350
                    :align-self :center
                    :padding-bottom 12
                    :padding-top 12
                    :margin-top 20
                    }
                  )

  [community-title-and-description title description image]

  [community-stats member_count likes_count dislike_count]

   [rn/touchable-opacity
    {:style {
              :height 40
              :width 320
              :flex-direction :row
              :align-self :center
              :align-items :center
              :justify-content :center
              :background-color "#EC4D57"
              :border-radius 10
              :padding-top 5
              :padding-right 12
              :padding-bottom 5
              :padding-left 12
              :margin-bottom 15
              }}

    [rn/view
      [icons/icon :i/communities
       {
         :size 20
         :color colors/white
         }
       ]
     ]

     [text/text {
                  :font-size 5
                  :font-weight 500
                  :font-style :normal
                  :padding-left 10
                  :weight :semi-bold
                  }
      "View Community"
     ]
    ]
   ]
  )

