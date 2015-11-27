/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.together._2006.xpil1.wrapper;


/**
 *
 * @author slavb
 */
public interface Wrapper {

    public <T extends Object> T unwrap(Class<T> type) throws WrapperNotCapableException;

    public boolean isWrapperFor(Class<?> type) throws WrapperNotCapableException;
}