public class ComplexMethod{public void postItem(Item a)throws ValidationException{if(a.isNew()){if(a.getX()!=null&&a.getY()!=null&&a.getZ()!=null){post(a)}else{throw new ValidationException("incomplete new object")}}else{if(a.getX()<10&&a.getY()>25&&a.getZ()>0){post(a)}else{throw new ValidationException("invalid update")}}}}
/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
public class SpiUtils {
    enum VisitType {
     INJECTOR,
     MODULE,
     BOTH
   }   static <T> void assertMapVisitor(
       Key<T> mapKey,
       TypeLiteral<?> keyType,
       TypeLiteral<?> valueType,
       Iterable<? extends Module> modules,
       VisitType visitType,
       boolean allowDuplicates,
       int expectedMapBindings,
       MapResult<?, ?>... results) {
     if (visitType == null) {
       fail("must test something");
     }     if (visitType == BOTH || visitType == INJECTOR) {
       mapInjectorTest(
           mapKey, keyType, valueType, modules, allowDuplicates, expectedMapBindings, results);
     }     if (visitType == BOTH || visitType == MODULE) {
       mapModuleTest(
           mapKey, keyType, valueType, modules, allowDuplicates, expectedMapBindings, results);}}
   @SuppressWarnings("unchecked")
  private static <T> void mapInjectorTest(
      Key<T> mapKey,
      TypeLiteral<?> keyType,
      TypeLiteral<?> valueType,
      Iterable<? extends Module> modules,
      boolean allowDuplicates,
      int expectedMapBindings,
      MapResult<?, ?>... results) {
    Injector injector = Guice.createInjector(modules);
    Visitor<T> visitor = new Visitor<>();
    Binding<T> mapBinding = injector.getBinding(mapKey);
    MapBinderBinding<T> mapbinder = (MapBinderBinding<T>) mapBinding.acceptTargetVisitor(visitor);
    assertNotNull(mapbinder);
    assertEquals(mapKey, mapbinder.getMapKey());
    assertEquals(keyType, mapbinder.getKeyTypeLiteral());
    assertEquals(valueType, mapbinder.getValueTypeLiteral());
    assertEquals(allowDuplicates, mapbinder.permitsDuplicates());
    List<Map.Entry<?, Binding<?>>> entries = Lists.newArrayList(mapbinder.getEntries());
    List<MapResult<?, ?>> mapResults = Lists.newArrayList(results);
    assertEquals(
        "wrong entries, expected: " + mapResults + ", but was: " + entries,
        mapResults.size(),
        entries.size());for (MapResult<?, ?> result : mapResults) {
      Map.Entry<?, Binding<?>> found = null;
      for (Map.Entry<?, Binding<?>> entry : entries) {
        Object key = entry.getKey();
        Binding<?> value = entry.getValue();
        if (key.equals(result.k) && matches(value, result.v)) {
          found = entry; break;}
      }if (found == null) {
        fail("Could not find entry: " + result + " in remaining entries: " + entries);
      } else {assertTrue(
            "mapBinder doesn't contain: " + found.getValue(),
            mapbinder.containsElement(found.getValue()));
        entries.remove(found);}} if (!entries.isEmpty()) {
      fail("Found all entries of: " + mapResults + ", but more were left over: " + entries);
    } Key<?> mapOfJavaxProvider = mapKey.ofType(mapOfJavaxProviderOf(keyType, valueType));
    Key<?> mapOfProvider = mapKey.ofType(mapOfProviderOf(keyType, valueType));
    Key<?> mapOfSetOfProvider = mapKey.ofType(mapOfSetOfProviderOf(keyType, valueType));
    Key<?> mapOfSetOfJavaxProvider = mapKey.ofType(mapOfSetOfJavaxProviderOf(keyType, valueType));
    Key<?> mapOfCollectionOfProvider =
        mapKey.ofType(mapOfCollectionOfProviderOf(keyType, valueType));
    Key<?> mapOfCollectionOfJavaxProvider =
        mapKey.ofType(mapOfCollectionOfJavaxProviderOf(keyType, valueType));
    Key<?> mapOfSet = mapKey.ofType(mapOf(keyType, setOf(valueType)));
    Key<?> setOfEntry = mapKey.ofType(setOf(entryOfProviderOf(keyType, valueType)));
    Key<?> setOfJavaxEntry = mapKey.ofType(setOf(entryOfJavaxProviderOf(keyType, valueType)));
    Key<?> collectionOfProvidersOfEntryOfProvider =
        mapKey.ofType(collectionOfProvidersOf(entryOfProviderOf(keyType, valueType)));
    Key<?> collectionOfJavaxProvidersOfEntryOfProvider =
        mapKey.ofType(collectionOfJavaxProvidersOf(entryOfProviderOf(keyType, valueType)));
    Key<?> setOfExtendsOfEntryOfProvider =
        mapKey.ofType(setOfExtendsOf(entryOfProviderOf(keyType, valueType)));
    Key<?> mapOfKeyExtendsValueKey =
        mapKey.ofType(mapOf(keyType, TypeLiteral.get(Types.subtypeOf(valueType.getType())))); assertEquals(
        ImmutableSet.of(
            mapOfJavaxProvider,
            mapOfProvider,
            mapOfSetOfProvider,
            mapOfSetOfJavaxProvider,
            mapOfCollectionOfProvider,
            mapOfCollectionOfJavaxProvider,
            mapOfSet,
            mapOfKeyExtendsValueKey),
        mapbinder.getAlternateMapKeys());
    boolean entrySetMatch = false;
    boolean javaxEntrySetMatch = false;
    boolean mapJavaxProviderMatch = false;
    boolean mapProviderMatch = false;
    boolean mapSetMatch = false;
    boolean mapSetProviderMatch = false;
    boolean mapSetJavaxProviderMatch = false;
    boolean mapCollectionProviderMatch = false;
    boolean mapCollectionJavaxProviderMatch = false;
    boolean collectionOfProvidersOfEntryOfProviderMatch = false;
    boolean collectionOfJavaxProvidersOfEntryOfProviderMatch = false;
    boolean setOfExtendsOfEntryOfProviderMatch = false;
    boolean mapOfKeyExtendsValueKeyMatch = false;
    List<Object> otherMapBindings = Lists.newArrayList();
    List<Binding<?>> otherMatches = Lists.newArrayList();
    Multimap<Object, IndexedBinding> indexedEntries =
        MultimapBuilder.hashKeys().hashSetValues().build();
    Indexer indexer = new Indexer(injector);
    int duplicates = 0;
    for (Binding<?> b : injector.getAllBindings().values()) {
      boolean contains = mapbinder.containsElement(b);
      Object visited = ((Binding<T>) b).acceptTargetVisitor(visitor);
      if (visited instanceof MapBinderBinding) {
        if (visited.equals(mapbinder)) {
          assertTrue(contains);
        } else {otherMapBindings.add(visited);}
      } else if (b.getKey().equals(mapOfProvider)) {
        assertTrue(contains);
        mapProviderMatch = true;
      } else if (b.getKey().equals(mapOfJavaxProvider)) {
        assertTrue(contains);
        mapJavaxProviderMatch = true;
      } else if (b.getKey().equals(mapOfSet)) {
        assertTrue(contains);
        mapSetMatch = true;
      } else if (b.getKey().equals(mapOfSetOfProvider)) {
        assertTrue(contains);
        mapSetProviderMatch = true;
      } else if (b.getKey().equals(mapOfSetOfJavaxProvider)) {
        assertTrue(contains);
        mapSetJavaxProviderMatch = true;
      } else if (b.getKey().equals(mapOfCollectionOfProvider)) {
        assertTrue(contains);
        mapCollectionProviderMatch = true;
      } else if (b.getKey().equals(mapOfCollectionOfJavaxProvider)) {
        assertTrue(contains);
        mapCollectionJavaxProviderMatch = true;
      } else if (b.getKey().equals(setOfEntry)) {
        assertTrue(contains);
        entrySetMatch = true;
        // Validate that this binding is also a MultibinderBinding.
        assertThat(((Binding<T>) b).acceptTargetVisitor(visitor))
            .isInstanceOf(MultibinderBinding.class);
      } else if (b.getKey().equals(setOfJavaxEntry)) {
        assertTrue(contains);
        javaxEntrySetMatch = true;
      } else if (b.getKey().equals(collectionOfProvidersOfEntryOfProvider)) {
        assertTrue(contains);
        collectionOfProvidersOfEntryOfProviderMatch = true;
      } else if (b.getKey().equals(collectionOfJavaxProvidersOfEntryOfProvider)) {
        assertTrue(contains);
        collectionOfJavaxProvidersOfEntryOfProviderMatch = true;
      } else if (b.getKey().equals(setOfExtendsOfEntryOfProvider)) {
        assertTrue(contains);
        setOfExtendsOfEntryOfProviderMatch = true;
      } else if (b.getKey().equals(mapOfKeyExtendsValueKey)) {
        assertTrue(contains);
        mapOfKeyExtendsValueKeyMatch = true;
      } else if (contains) {
        if (b instanceof ProviderInstanceBinding) {
          ProviderInstanceBinding<?> pib = (ProviderInstanceBinding<?>) b;
          if (pib.getUserSuppliedProvider() instanceof ProviderMapEntry) {
            // weird casting required to workaround compilation issues with jdk6
            ProviderMapEntry<?, ?> pme =
                (ProviderMapEntry<?, ?>) (Provider) pib.getUserSuppliedProvider();
            Binding<?> valueBinding = injector.getBinding(pme.getValueKey());
            if (indexer.isIndexable(valueBinding)
                && !indexedEntries.put(pme.getKey(), valueBinding.acceptTargetVisitor(indexer))) {
              duplicates++;}}}otherMatches.add(b);}}int sizeOfOther = otherMatches.size();
    if (allowDuplicates) {
      sizeOfOther--; // account for 1 duplicate binding
    }    int expectedSize = 2 * (mapResults.size() + duplicates);
    assertEquals( "Incorrect other matches:\n\t" + Joiner.on("\n\t").join(otherMatches),
        expectedSize,sizeOfOther);
    assertTrue(entrySetMatch);
    assertTrue(javaxEntrySetMatch);
    assertTrue(mapProviderMatch);
    assertTrue(mapJavaxProviderMatch);
    assertTrue(collectionOfProvidersOfEntryOfProviderMatch);
    assertTrue(collectionOfJavaxProvidersOfEntryOfProviderMatch);
    assertTrue(setOfExtendsOfEntryOfProviderMatch);
    assertTrue(mapOfKeyExtendsValueKeyMatch);
    assertEquals(allowDuplicates, mapSetMatch);
    assertEquals(allowDuplicates, mapSetProviderMatch);
    assertEquals(allowDuplicates, mapSetJavaxProviderMatch);
    assertEquals(allowDuplicates, mapCollectionJavaxProviderMatch);
    assertEquals(allowDuplicates, mapCollectionProviderMatch);
    assertEquals(
        "other MapBindings found: " + otherMapBindings,expectedMapBindings,otherMapBindings.size()); }
  @SuppressWarnings("unchecked")
  private static <T> void mapModuleTest(
      Key<T> mapKey,
      TypeLiteral<?> keyType,
      TypeLiteral<?> valueType,
      Iterable<? extends Module> modules,
      boolean allowDuplicates,
      int expectedMapBindings,
      MapResult<?, ?>... results) {
    Set<Element> elements = ImmutableSet.copyOf(Elements.getElements(modules));
    Visitor<T> visitor = new Visitor<>();
    MapBinderBinding<T> mapbinder = null;
    Map<Key<?>, Binding<?>> keyMap = Maps.newHashMap();
    for (Element element : elements) {
      if (element instanceof Binding) {
        Binding<?> binding = (Binding<?>) element;
        keyMap.put(binding.getKey(), binding);
        if (binding.getKey().equals(mapKey)) {
          mapbinder = (MapBinderBinding<T>) ((Binding<T>) binding).acceptTargetVisitor(visitor);}}}
    assertNotNull(mapbinder);
    List<MapResult<?, ?>> mapResults = Lists.newArrayList(results);
    List<Map.Entry<?, Binding<?>>> entries = Lists.newArrayList(mapbinder.getEntries(elements));
    for (MapResult<?, ?> result : mapResults) {
      List<Map.Entry<?, Binding<?>>> foundEntries = Lists.newArrayList();
      for (Map.Entry<?, Binding<?>> entry : entries) {
        Object key = entry.getKey();
        Binding<?> value = entry.getValue();
        if (key.equals(result.k) && matches(value, result.v)) {
          assertTrue(
              "mapBinder doesn't contain: " + entry.getValue(),
              mapbinder.containsElement(entry.getValue()));
          foundEntries.add(entry);}}assertTrue(
          "Could not find entry: " + result + " in remaining entries: " + entries,
          !foundEntries.isEmpty()); entries.removeAll(foundEntries);} assertTrue(
        "Found all entries of: " + mapResults + ", but more were left over: " + entries,
        entries.isEmpty());
    assertEquals(mapKey, mapbinder.getMapKey());
    assertEquals(keyType, mapbinder.getKeyTypeLiteral());
    assertEquals(valueType, mapbinder.getValueTypeLiteral());
    Key<?> mapOfProvider = mapKey.ofType(mapOfProviderOf(keyType, valueType));
    Key<?> mapOfJavaxProvider = mapKey.ofType(mapOfJavaxProviderOf(keyType, valueType));
    Key<?> mapOfSetOfProvider = mapKey.ofType(mapOfSetOfProviderOf(keyType, valueType));
    Key<?> mapOfSetOfJavaxProvider = mapKey.ofType(mapOfSetOfJavaxProviderOf(keyType, valueType));
    Key<?> mapOfCollectionOfProvider =
        mapKey.ofType(mapOfCollectionOfProviderOf(keyType, valueType));
    Key<?> mapOfCollectionOfJavaxProvider =
        mapKey.ofType(mapOfCollectionOfJavaxProviderOf(keyType, valueType));
    Key<?> mapOfSet = mapKey.ofType(mapOf(keyType, setOf(valueType)));
    Key<?> setOfEntry = mapKey.ofType(setOf(entryOfProviderOf(keyType, valueType)));
    Key<?> setOfJavaxEntry = mapKey.ofType(setOf(entryOfJavaxProviderOf(keyType, valueType)));
    Key<?> collectionOfProvidersOfEntryOfProvider =
        mapKey.ofType(collectionOfProvidersOf(entryOfProviderOf(keyType, valueType)));
    Key<?> collectionOfJavaxProvidersOfEntryOfProvider =
        mapKey.ofType(collectionOfJavaxProvidersOf(entryOfProviderOf(keyType, valueType)));
    Key<?> setOfExtendsOfEntryOfProvider =
        mapKey.ofType(setOfExtendsOf(entryOfProviderOf(keyType, valueType)));
    Key<?> mapOfKeyExtendsValueKey =
        mapKey.ofType(mapOf(keyType, TypeLiteral.get(Types.subtypeOf(valueType.getType()))));
    assertEquals(
        ImmutableSet.of(mapOfProvider,mapOfJavaxProvider,mapOfSetOfProvider,mapOfSetOfJavaxProvider,mapOfCollectionOfProvider,mapOfCollectionOfJavaxProvider,mapOfSet,mapOfKeyExtendsValueKey),
        mapbinder.getAlternateMapKeys());
    boolean entrySetMatch = false;
    boolean entrySetJavaxMatch = false;
    boolean mapProviderMatch = false;}
package com.google.inject.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.PrivateBinder;
import com.google.inject.PrivateModule;
import com.google.inject.Scope;
import com.google.inject.internal.Errors;
import com.google.inject.spi.DefaultBindingScopingVisitor;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Element;
import com.google.inject.spi.ElementVisitor;
import com.google.inject.spi.Elements;
import com.google.inject.spi.ModuleAnnotatedMethodScannerBinding;
import com.google.inject.spi.PrivateElements;
import com.google.inject.spi.ScopeBinding;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Static utility methods for creating and working with instances of {@link Module}.
 *
 * @author jessewilson@google.com (Jesse Wilson)
 * @since 2.0
 */
@CheckReturnValue
public final class Modules {
  private Modules() {}

  public static final Module EMPTY_MODULE = new EmptyModule();

  private static class EmptyModule implements Module {
    @Override
    public void configure(Binder binder) {}
  }

  /**
   * Returns a builder that creates a module that overlays override modules over the given modules.
   * If a key is bound in both sets of modules, only the binding from the override modules is kept.
   * If a single {@link PrivateModule} is supplied or all elements are from a single {@link
   * PrivateBinder}, then this will overwrite the private bindings. Otherwise, private bindings will
   * not be overwritten unless they are exposed. This can be used to replace the bindings of a
   * production module with test bindings:
   *
   * <pre>
   * Module functionalTestModule
   *     = Modules.override(new ProductionModule()).with(new TestModule());
   * </pre>
   *
   * <p>Prefer to write smaller modules that can be reused and tested without overrides.
   *
   * @param modules the modules whose bindings are open to be overridden
   */
  public static OverriddenModuleBuilder override(Module... modules) {
    return override(Arrays.asList(modules));
  }

  /** @deprecated there's no reason to use {@code Modules.override()} without any arguments. */
  @Deprecated
  public static OverriddenModuleBuilder override() {
    return override(Arrays.asList());
  }

  /**
   * Returns a builder that creates a module that overlays override modules over the given modules.
   * If a key is bound in both sets of modules, only the binding from the override modules is kept.
   * If a single {@link PrivateModule} is supplied or all elements are from a single {@link
   * PrivateBinder}, then this will overwrite the private bindings. Otherwise, private bindings will
   * not be overwritten unless they are exposed. This can be used to replace the bindings of a
   * production module with test bindings:
   *
   * <pre>
   * Module functionalTestModule
   *     = Modules.override(getProductionModules()).with(getTestModules());
   * </pre>
   *
   * <p>Prefer to write smaller modules that can be reused and tested without overrides.
   *
   * @param modules the modules whose bindings are open to be overridden
   */
  public static OverriddenModuleBuilder override(Iterable<? extends Module> modules) {
    return new RealOverriddenModuleBuilder(modules);
  }

  /**
   * Returns a new module that installs all of {@code modules}.
   *
   * <p>Although sometimes helpful, this method is rarely necessary. Most Guice APIs accept multiple
   * arguments or (like {@code install()}) can be called repeatedly. Where possible, external APIs
   * that require a single module should similarly be adapted to permit multiple modules.
   */
  public static Module combine(Module... modules) {
    return combine(ImmutableSet.copyOf(modules));
  }

  /** @deprecated there's no need to "combine" one module; just install it directly. */
  @Deprecated
  public static Module combine(Module module) {
    return module;
  }

  /** @deprecated this method call is effectively a no-op, just remove it. */
  @Deprecated
  public static Module combine() {
    return EMPTY_MODULE;
  }

  /**
   * Returns a new module that installs all of {@code modules}.
   *
   * <p>Although sometimes helpful, this method is rarely necessary. Most Guice APIs accept multiple
   * arguments or (like {@code install()}) can be called repeatedly. Where possible, external APIs
   * that require a single module should similarly be adapted to permit multiple modules.
   */
  public static Module combine(Iterable<? extends Module> modules) {
    return new CombinedModule(modules);
  }

  private static class CombinedModule implements Module {
    final Set<Module> modulesSet;

    CombinedModule(Iterable<? extends Module> modules) {
      this.modulesSet = ImmutableSet.copyOf(modules);
    }

    @Override
    public void configure(Binder binder) {
      binder = binder.skipSources(getClass());
      for (Module module : modulesSet) {
        binder.install(module);
      }
    }
  }

  /** See the EDSL example at {@link Modules#override(Module[]) override()}. */
  public interface OverriddenModuleBuilder {

    /** See the EDSL example at {@link Modules#override(Module[]) override()}. */
    Module with(Module... overrides);

    /** @deprecated there's no reason to use {@code .with()} without any arguments. */
    @Deprecated
    public Module with();

    /** See the EDSL example at {@link Modules#override(Module[]) override()}. */
    Module with(Iterable<? extends Module> overrides);
  }

  private static final class RealOverriddenModuleBuilder implements OverriddenModuleBuilder {
    private final ImmutableSet<Module> baseModules;

    // TODO(diamondm) checkArgument(!baseModules.isEmpty())?
    private RealOverriddenModuleBuilder(Iterable<? extends Module> baseModules) {
      this.baseModules = ImmutableSet.copyOf(baseModules);
    }

    @Override
    public Module with(Module... overrides) {
      return with(Arrays.asList(overrides));
    }

    @Override
    public Module with() {
      return with(Arrays.asList());
    }

    @Override
    public Module with(Iterable<? extends Module> overrides) {
      return new OverrideModule(overrides, baseModules);
    }
  }

  static class OverrideModule extends AbstractModule {
    private final ImmutableSet<Module> overrides;
    private final ImmutableSet<Module> baseModules;

    // TODO(diamondm) checkArgument(!overrides.isEmpty())?
    OverrideModule(Iterable<? extends Module> overrides, ImmutableSet<Module> baseModules) {
      this.overrides = ImmutableSet.copyOf(overrides);
      this.baseModules = baseModules;
    }

    @Override
    public void configure() {
      Binder baseBinder = binder();
      List<Element> baseElements = Elements.getElements(currentStage(), baseModules);

      // If the sole element was a PrivateElements, we want to override
      // the private elements within that -- so refocus our elements
      // and binder.
      if (baseElements.size() == 1) {
        Element element = Iterables.getOnlyElement(baseElements);
        if (element instanceof PrivateElements) {
          PrivateElements privateElements = (PrivateElements) element;
          PrivateBinder privateBinder =
              baseBinder.newPrivateBinder().withSource(privateElements.getSource());
          for (Key<?> exposed : privateElements.getExposedKeys()) {
            privateBinder.withSource(privateElements.getExposedSource(exposed)).expose(exposed);
          }
          baseBinder = privateBinder;
          baseElements = privateElements.getElements();
        }
      }

      final Binder binder = baseBinder.skipSources(this.getClass());
      final ImmutableSet<Element> elements = ImmutableSet.copyOf(baseElements);
      final Module scannersModule = extractScanners(elements);
      final List<Element> overrideElements =
          Elements.getElements(
              currentStage(),
              ImmutableList.<Module>builder().addAll(overrides).add(scannersModule).build());

      final Set<Key<?>> overriddenKeys = Sets.newHashSet();
      final Map<Class<? extends Annotation>, ScopeBinding> overridesScopeAnnotations =
          Maps.newHashMap();

      // execute the overrides module, keeping track of which keys and scopes are bound
      new ModuleWriter(binder) {
        @Override
        public <T> Void visit(Binding<T> binding) {
          overriddenKeys.add(binding.getKey());
          return super.visit(binding);
        }

        @Override
        public Void visit(ScopeBinding scopeBinding) {
          overridesScopeAnnotations.put(scopeBinding.getAnnotationType(), scopeBinding);
          return super.visit(scopeBinding);
        }

        @Override
        public Void visit(PrivateElements privateElements) {
          overriddenKeys.addAll(privateElements.getExposedKeys());
          return super.visit(privateElements);
        }
      }.writeAll(overrideElements);

      // execute the original module, skipping all scopes and overridden keys. We only skip each
      // overridden binding once so things still blow up if the module binds the same thing
      // multiple times.
      final Map<Scope, List<Object>> scopeInstancesInUse = Maps.newHashMap();
      final List<ScopeBinding> scopeBindings = Lists.newArrayList();
      new ModuleWriter(binder) {
        @Override
        public <T> Void visit(Binding<T> binding) {
          if (!overriddenKeys.remove(binding.getKey())) {
            super.visit(binding);

            // Record when a scope instance is used in a binding
            Scope scope = getScopeInstanceOrNull(binding);
            if (scope != null) {
              scopeInstancesInUse
                  .computeIfAbsent(scope, k -> Lists.newArrayList())
                  .add(binding.getSource());
            }
          }

          return null;
        }

        void rewrite(Binder binder, PrivateElements privateElements, Set<Key<?>> keysToSkip) {
          PrivateBinder privateBinder =
              binder.withSource(privateElements.getSource()).newPrivateBinder();

          Set<Key<?>> skippedExposes = Sets.newHashSet();

          for (Key<?> key : privateElements.getExposedKeys()) {
            if (keysToSkip.remove(key)) {
              skippedExposes.add(key);
            } else {
              privateBinder.withSource(privateElements.getExposedSource(key)).expose(key);
            }
          }

          for (Element element : privateElements.getElements()) {
            if (element instanceof Binding && skippedExposes.remove(((Binding) element).getKey())) {
              continue;
            }
            if (element instanceof PrivateElements) {
              rewrite(privateBinder, (PrivateElements) element, skippedExposes);
              continue;
            }
            element.applyTo(privateBinder);
          }
        }

        @Override
        public Void visit(PrivateElements privateElements) {
          rewrite(binder, privateElements, overriddenKeys);
          return null;
        }

        @Override
        public Void visit(ScopeBinding scopeBinding) {
          scopeBindings.add(scopeBinding);
          return null;
        }
      }.writeAll(elements);

      // execute the scope bindings, skipping scopes that have been overridden. Any scope that
      // is overridden and in active use will prompt an error
      new ModuleWriter(binder) {
        @Override
        public Void visit(ScopeBinding scopeBinding) {
          ScopeBinding overideBinding =
              overridesScopeAnnotations.remove(scopeBinding.getAnnotationType());
          if (overideBinding == null) {
            super.visit(scopeBinding);
          } else {
            List<Object> usedSources = scopeInstancesInUse.get(scopeBinding.getScope());
            if (usedSources != null) {
              @SuppressWarnings("OrphanedFormatString") // passed to format method addError below
              StringBuilder sb =
                  new StringBuilder(
                      "The scope for @%s is bound directly and cannot be overridden.");
              sb.append("%n     original binding at " + Errors.convert(scopeBinding.getSource()));
              for (Object usedSource : usedSources) {
                sb.append("%n     bound directly at " + Errors.convert(usedSource) + "");
              }
              binder
                  .withSource(overideBinding.getSource())
                  .addError(sb.toString(), scopeBinding.getAnnotationType().getSimpleName());
            }
          }
          return null;
        }
      }.writeAll(scopeBindings);
    }

    private Scope getScopeInstanceOrNull(Binding<?> binding) {
      return binding.acceptScopingVisitor(
          new DefaultBindingScopingVisitor<Scope>() {
            @Override
            public Scope visitScope(Scope scope) {
              return scope;
            }
          });
    }
  }

  private static class ModuleWriter extends DefaultElementVisitor<Void> {
    protected final Binder binder;

    ModuleWriter(Binder binder) {
      this.binder = binder.skipSources(this.getClass());
    }

    @Override
    protected Void visitOther(Element element) {
      element.applyTo(binder);
      return null;
    }

    void writeAll(Iterable<? extends Element> elements) {
      for (Element element : elements) {
        element.acceptVisitor(this);
      }
    }
  }

  private static Module extractScanners(Iterable<Element> elements) {
    final List<ModuleAnnotatedMethodScannerBinding> scanners = Lists.newArrayList();
    ElementVisitor<Void> visitor =
        new DefaultElementVisitor<Void>() {
          @Override
          public Void visit(ModuleAnnotatedMethodScannerBinding binding) {
            scanners.add(binding);
            return null;
          }
        };
    for (Element element : elements) {
      element.acceptVisitor(visitor);
    }
    return new AbstractModule() {
      @Override
      protected void configure() {
        for (ModuleAnnotatedMethodScannerBinding scanner : scanners) {
          scanner.applyTo(binder());
        }
      }
    };
  }

  /**
   * Returns a module that will configure the injector to require explicit bindings.
   *
   * @since 4.2.3
   */
  public static Module requireExplicitBindingsModule() {
    return new RequireExplicitBindingsModule();
  }

  private static final class RequireExplicitBindingsModule implements Module {
    @Override
    public void configure(Binder binder) {
      binder.requireExplicitBindings();
    }
  }

  /**
   * Returns a module that will configure the injector to require {@literal @}{@link Inject} on
   * constructors.
   *
   * @since 4.2.3
   * @see Binder#requireAtInjectOnConstructors
   */
  public static Module requireAtInjectOnConstructorsModule() {
    return new RequireAtInjectOnConstructorsModule();
  }

  private static final class RequireAtInjectOnConstructorsModule implements Module {
    @Override
    public void configure(Binder binder) {
      binder.requireAtInjectOnConstructors();
    }
  }

  /**
   * Returns a module that will configure the injector to require an exactly matching binding
   * annotation.
   *
   * @since 4.2.3
   * @see Binder#requireExactBindingAnnotations
   */
  public static Module requireExactBindingAnnotationsModule() {
    return new RequireExactBindingAnnotationsModule();
  }

  private static final class RequireExactBindingAnnotationsModule implements Module {
    @Override
    public void configure(Binder binder) {
      binder.requireExactBindingAnnotations();
    }
  }

  /**
   * Returns a module that will configure the injector to disable circular proxies.
   *
   * @since 4.2.3
   */
  public static Module disableCircularProxiesModule() {
    return new DisableCircularProxiesModule();
  }

  private static final class DisableCircularProxiesModule implements Module {
    @Override
    public void configure(Binder binder) {
      binder.disableCircularProxies();
    }
  }
}
