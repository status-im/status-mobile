(ns status-im2.common.contact-list.view
  (:require [quo2.core :as quo]))

(defn contacts-section-header
  [{:keys [title]}]
  [quo/divider-label title])
