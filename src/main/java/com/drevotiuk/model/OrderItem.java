package com.drevotiuk.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class OrderItem {
  private String productId;
  private int qty;
}
