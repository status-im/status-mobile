(ns status-im2.contexts.wallet.edit-account.style)

(defn root-container
  [top]
  {:flex       1
   :margin-top top})

(defn gradient-cover-container
  [top]
  {:position :absolute
   :top      (- top)
   :left     0
   :right    0
   :z-index  -1})

(def account-avatar-container
  {:padding-horizontal 20
   :padding-top        12})

(def reaction-button-container
  {:position :absolute
   :bottom   0
   :left     80})

(def title-input-container
  {:padding-horizontal 20
   :padding-top        12
   :padding-bottom     16})

(def divider-1
  {:margin-bottom 12})

(def section-container
  {:padding-horizontal 20
   :padding-bottom     4})

(def color-picker-container
  {:padding-vertical 12})

(def color-picker
  {:padding-horizontal 12})

(def divider-2
  {:margin-top    4
   :margin-bottom 12})

(def data-item
  {:margin-horizontal 20
   :margin-vertical   8})
