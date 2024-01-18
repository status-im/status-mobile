(ns status-im.common.contact-list.style)

(defn contacts-section-header
  [first-item?]
  {:padding-top (if first-item? 0 8)})
