(ns status-im.contexts.wallet.send.save-address.style)

(def title-input-container
  {:padding-horizontal 20
   :padding-top        12
   :padding-bottom     16})

(def account-avatar-container
  {:padding-horizontal 20
   :padding-top        12})

(def divider-1
  {:margin-bottom 12})

(def color-picker-container
  {:padding-vertical 12})

(def section-container
  {:padding-horizontal 20
   :padding-bottom     4})

(def color-picker
  {:padding-horizontal 20
   :padding-top        12})

(def divider-2
  {:padding-top    8
   :padding-bottom 12})

(def save-address-button
  {:padding-horizontal 20})

(def data-item
  {:margin-horizontal 20
   :padding-vertical  8})

(def address-container
  {:padding-top    8
   :padding-bottom 12})

(def container
  {:flex         1
   ;; This negative margin is needed because bottom sheet as a modal
   ;; doesn't play well with `:sheet? true`
   :margin-top   -20
   :border-width 0
   :border-color :red})
