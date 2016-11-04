(ns status-im.transactions.styles)

(def transactions-screen
  {:flex 1
   :backgroundColor "#828b92"})

(def transactions-toolbar
  {:backgroundColor "#828b92"
   :elevation 0})

(def toolbar-title-text
  {:color :white
   :fontSize 16})

(def carousel-page-style
  {})

(def form-container
  {:flex 1
   :paddingLeft 16})

(def password-style
  {:color     :white
   :font-size 12})

;transaction-page

(def transaction-page
  {:flex 1
   :backgroundColor "#f3f4f4"})

(def title-bar
  {:backgroundColor :white
   :height 39
   :justifyContent :center})

(def title-bar-text
  {:color        "#838c93"
   :font-size    13
   :margin-left  12
   :margin-right 30})

(def icon-close-container
  {:position :absolute
   :right 12
   :top 13})

(def icon-close
  {:width  12
   :height 12})

(def transaction-info-container
  {:flex 1
   :paddingTop 6})

(def scroll-view-container
  {:flex 1})

(def scroll-view
  {:flex 1
   :height 175})

(def scroll-view-content
  {:paddingVertical 6})

(def transaction-info-row
  {:flex 1
   :flexDirection :row})

(def transaction-info-column-title
  {:flex 0.4
   :flexDirection :column
   :paddingHorizontal 6})

(def transaction-info-column-value
  {:flex 0.6
   :flexDirection :column
   :paddingHorizontal 6})

(def transaction-info-item
  {:flex 1
   :padding 6})

(def transaction-info-title
  {:textAlign :right
   :color "#838c93de"
   :fontSize 14
   :lineHeight 20})

(def transaction-info-value
  {:color "#000000de"
   :fontSize 14
   :lineHeight 20
})

(def scroll-view-item
  {:flex 1
   :height 20
   :padding 6 })

(def carousel-container
  {:min-height 215
   :flex       1})
