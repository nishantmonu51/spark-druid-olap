/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.util

import org.apache.spark.sql.catalyst.expressions._


object ExprUtil {

  /**
    * If any input col/ref is null then expression will evaluate to null
    * and if no input col/ref is null then expression won't evaluate to null
    *
    * @param e
    * @return
    */
  def nullPreserving(e: Expression): Boolean = e match {
    case _ if e.isInstanceOf[LeafExpression] => true
      // TODO: Expand the case below
    case (Cast(_, _) | BinaryArithmetic(_) | UnaryMinus(_) | UnaryPositive(_) | Abs(_))
    => e.children.filter(_.isInstanceOf[Expression]).foldLeft(true) {
      (s,ce) => val cexp = nullPreserving(ce); if (s && cexp) return true else false }
    case _ => false
  }

  def conditionalExpr(e: Expression) : Boolean = {
    e match {
      case If(_,_,_) | CaseWhen(_) | CaseKeyWhen(_,_) | Least(_) | Greatest(_)
           | Coalesce(_) | NaNvl(_, _) | AtLeastNNonNulls(_, _) => true
      case _ if e.isInstanceOf[LeafExpression] => false
      case _ => {
        e.children.filter(_.isInstanceOf[Expression]).foldLeft(false){
          (s,ce) => val cexp = conditionalExpr(ce); if (s ||cexp) return true else false }
      }
    }
  }
}